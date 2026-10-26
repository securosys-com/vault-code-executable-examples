// SPDX-License-Identifier: Apache-2.0

package wasisocket

import (
	"fmt"
	"io"
	"net"
	"sync"
	"time"

	streams "securosys/tee-docker/examples/database-wasm/bindings/wasi_io_0_2_8_streams"
	networkapi "securosys/tee-docker/examples/database-wasm/bindings/wasi_sockets_0_2_8_network"
	tcpapi "securosys/tee-docker/examples/database-wasm/bindings/wasi_sockets_0_2_8_tcp"
)

type Conn struct {
	socket *tcpapi.TcpSocket
	input  *streams.InputStream
	output *streams.OutputStream
	once   sync.Once
}

func (c *Conn) Read(p []byte) (int, error) {
	if len(p) == 0 {
		return 0, nil
	}
	result := c.input.BlockingRead(uint64(len(p)))
	if result.IsErr() {
		if result.Err().Tag() == streams.StreamErrorClosed {
			return 0, io.EOF
		}
		return 0, fmt.Errorf("WASI socket read failed")
	}
	data := result.Ok()
	if len(data) == 0 {
		return 0, io.EOF
	}
	return copy(p, data), nil
}

func (c *Conn) Write(p []byte) (int, error) {
	if len(p) == 0 {
		return 0, nil
	}
	result := c.output.BlockingWriteAndFlush(p)
	if result.IsErr() {
		return 0, fmt.Errorf("WASI socket write failed")
	}
	return len(p), nil
}

func (c *Conn) Close() error {
	c.once.Do(func() {
		_ = c.socket.Shutdown(tcpapi.ShutdownTypeBoth)
		c.input.Drop()
		c.output.Drop()
		c.socket.Drop()
	})
	return nil
}

func (c *Conn) LocalAddr() net.Addr  { return socketAddr(c.socket.LocalAddress()) }
func (c *Conn) RemoteAddr() net.Addr { return socketAddr(c.socket.RemoteAddress()) }

// WASI 0.2 streams do not expose per-operation deadlines. Callers should use
// Wasmtime epoch interruption/fuel or the embedding application's timeout.
func (c *Conn) SetDeadline(time.Time) error      { return nil }
func (c *Conn) SetReadDeadline(time.Time) error  { return nil }
func (c *Conn) SetWriteDeadline(time.Time) error { return nil }

type addr string

func (a addr) Network() string { return "tcp" }
func (a addr) String() string  { return string(a) }

func socketAddr(result interface {
	IsErr() bool
	Ok() networkapi.IpSocketAddress
}) net.Addr {
	if result.IsErr() {
		return addr("unknown")
	}
	value := result.Ok()
	if value.Tag() == networkapi.IpSocketAddressIpv4 {
		v := value.Ipv4()
		ip := net.IPv4(v.Address.F0, v.Address.F1, v.Address.F2, v.Address.F3)
		return addr(net.JoinHostPort(ip.String(), fmt.Sprint(v.Port)))
	}
	v := value.Ipv6()
	ip := net.IP{byte(v.Address.F0 >> 8), byte(v.Address.F0), byte(v.Address.F1 >> 8), byte(v.Address.F1), byte(v.Address.F2 >> 8), byte(v.Address.F2), byte(v.Address.F3 >> 8), byte(v.Address.F3), byte(v.Address.F4 >> 8), byte(v.Address.F4), byte(v.Address.F5 >> 8), byte(v.Address.F5), byte(v.Address.F6 >> 8), byte(v.Address.F6), byte(v.Address.F7 >> 8), byte(v.Address.F7)}
	return addr(net.JoinHostPort(ip.String(), fmt.Sprint(v.Port)))
}

var _ net.Conn = (*Conn)(nil)
