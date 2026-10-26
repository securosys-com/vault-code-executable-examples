// SPDX-License-Identifier: Apache-2.0

package wasisocket

import (
	"context"
	"encoding/binary"
	"fmt"
	"net"
	"strconv"
	"strings"

	witTypes "go.bytecodealliance.org/pkg/wit/types"
	instance "securosys/tee-docker/examples/database-wasm/bindings/wasi_sockets_0_2_8_instance_network"
	iplookup "securosys/tee-docker/examples/database-wasm/bindings/wasi_sockets_0_2_8_ip_name_lookup"
	networkapi "securosys/tee-docker/examples/database-wasm/bindings/wasi_sockets_0_2_8_network"
	tcpapi "securosys/tee-docker/examples/database-wasm/bindings/wasi_sockets_0_2_8_tcp"
	create "securosys/tee-docker/examples/database-wasm/bindings/wasi_sockets_0_2_8_tcp_create_socket"
)

// LookupHost resolves hostnames through WASI sockets for pgx.
func LookupHost(ctx context.Context, host string) ([]string, error) {
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	if addresses, ok := lookupLiteralHost(host); ok {
		return addresses, nil
	}

	networkResource := instance.InstanceNetwork()
	defer networkResource.Drop()
	addresses, err := resolve(networkResource, host)
	if err != nil {
		return nil, err
	}
	resolved := make([]string, 0, len(addresses))
	for _, address := range addresses {
		resolved = append(resolved, ipFromAddress(address).String())
	}
	return resolved, nil
}

// DialContext creates a net.Conn backed entirely by wasi:sockets@0.2.8.
func DialContext(ctx context.Context, network, address string) (net.Conn, error) {
	if network != "tcp" && network != "tcp4" && network != "tcp6" {
		return nil, fmt.Errorf("wasisocket: unsupported network %q", network)
	}
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	host, portText, err := net.SplitHostPort(address)
	if err != nil {
		return nil, fmt.Errorf("wasisocket: invalid address: %w", err)
	}
	port64, err := strconv.ParseUint(portText, 10, 16)
	if err != nil || port64 == 0 {
		return nil, fmt.Errorf("wasisocket: invalid port %q", portText)
	}

	networkResource := instance.InstanceNetwork()
	defer networkResource.Drop()
	addresses, err := resolve(networkResource, host)
	if err != nil {
		return nil, err
	}
	var lastErr error
	for _, ip := range addresses {
		if network == "tcp4" && ip.Tag() != networkapi.IpAddressIpv4 {
			continue
		}
		if network == "tcp6" && ip.Tag() != networkapi.IpAddressIpv6 {
			continue
		}
		conn, dialErr := connect(ctx, networkResource, ip, uint16(port64))
		if dialErr == nil {
			return conn, nil
		}
		lastErr = dialErr
	}
	if lastErr == nil {
		lastErr = fmt.Errorf("no suitable address")
	}
	return nil, fmt.Errorf("wasisocket: connect %s: %w", address, lastErr)
}

func resolve(networkResource *networkapi.Network, host string) ([]networkapi.IpAddress, error) {
	if ips, ok := literalAddresses(host); ok {
		addresses := make([]networkapi.IpAddress, 0, len(ips))
		for _, ip := range ips {
			addresses = append(addresses, ipAddress(ip))
		}
		return addresses, nil
	}
	result := iplookup.ResolveAddresses(networkResource, host)
	if result.IsErr() {
		return nil, socketError("resolve", result.Err())
	}
	stream := result.Ok()
	defer stream.Drop()
	var addresses []networkapi.IpAddress
	for {
		next := stream.ResolveNextAddress()
		if next.IsErr() {
			if next.Err() == networkapi.ErrorCodeWouldBlock {
				pollable := stream.Subscribe()
				pollable.Block()
				pollable.Drop()
				continue
			}
			return nil, socketError("resolve-next-address", next.Err())
		}
		if next.Ok().IsNone() {
			break
		}
		addresses = append(addresses, next.Ok().Some())
	}
	return addresses, nil
}

func literalAddresses(host string) ([]net.IP, bool) {
	if ip := net.ParseIP(host); ip != nil {
		return []net.IP{ip}, true
	}
	if strings.EqualFold(strings.TrimSuffix(host, "."), "localhost") {
		return []net.IP{net.IPv4(127, 0, 0, 1), net.ParseIP("::1")}, true
	}
	return nil, false
}

func lookupLiteralHost(host string) ([]string, bool) {
	ips, ok := literalAddresses(host)
	if !ok {
		return nil, false
	}
	addresses := make([]string, 0, len(ips))
	for _, ip := range ips {
		addresses = append(addresses, ip.String())
	}
	return addresses, true
}

func ipAddress(ip net.IP) networkapi.IpAddress {
	if v4 := ip.To4(); v4 != nil {
		return networkapi.MakeIpAddressIpv4(witTypes.Tuple4[uint8, uint8, uint8, uint8]{F0: v4[0], F1: v4[1], F2: v4[2], F3: v4[3]})
	}
	v6 := ip.To16()
	word := func(i int) uint16 { return uint16(v6[i])<<8 | uint16(v6[i+1]) }
	return networkapi.MakeIpAddressIpv6(witTypes.Tuple8[uint16, uint16, uint16, uint16, uint16, uint16, uint16, uint16]{F0: word(0), F1: word(2), F2: word(4), F3: word(6), F4: word(8), F5: word(10), F6: word(12), F7: word(14)})
}

func ipFromAddress(address networkapi.IpAddress) net.IP {
	if address.Tag() == networkapi.IpAddressIpv4 {
		ip := address.Ipv4()
		return net.IP{ip.F0, ip.F1, ip.F2, ip.F3}
	}
	ip := address.Ipv6()
	result := make(net.IP, net.IPv6len)
	for i, word := range [...]uint16{ip.F0, ip.F1, ip.F2, ip.F3, ip.F4, ip.F5, ip.F6, ip.F7} {
		binary.BigEndian.PutUint16(result[i*2:], word)
	}
	return result
}

func socketAddress(ip networkapi.IpAddress, port uint16) networkapi.IpSocketAddress {
	if ip.Tag() == networkapi.IpAddressIpv4 {
		return networkapi.MakeIpSocketAddressIpv4(networkapi.Ipv4SocketAddress{Port: port, Address: ip.Ipv4()})
	}
	return networkapi.MakeIpSocketAddressIpv6(networkapi.Ipv6SocketAddress{Port: port, Address: ip.Ipv6()})
}

func connect(ctx context.Context, networkResource *networkapi.Network, ip networkapi.IpAddress, port uint16) (*Conn, error) {
	family := networkapi.IpAddressFamilyIpv4
	if ip.Tag() == networkapi.IpAddressIpv6 {
		family = networkapi.IpAddressFamilyIpv6
	}
	created := create.CreateTcpSocket(family)
	if created.IsErr() {
		return nil, socketError("create-tcp-socket", created.Err())
	}
	socket := created.Ok()
	started := socket.StartConnect(networkResource, socketAddress(ip, port))
	if started.IsErr() {
		socket.Drop()
		return nil, socketError("start-connect", started.Err())
	}
	for {
		if err := ctx.Err(); err != nil {
			socket.Drop()
			return nil, err
		}
		finished := socket.FinishConnect()
		if finished.IsOk() {
			streams := finished.Ok()
			return &Conn{socket: socket, input: streams.F0, output: streams.F1}, nil
		}
		if finished.Err() != networkapi.ErrorCodeWouldBlock {
			socket.Drop()
			return nil, socketError("finish-connect", finished.Err())
		}
		pollable := socket.Subscribe()
		pollable.Block()
		pollable.Drop()
	}
}

func socketError(operation string, code networkapi.ErrorCode) error {
	return fmt.Errorf("%s failed with WASI socket error code %d", operation, code)
}

var _ = tcpapi.ShutdownTypeBoth
