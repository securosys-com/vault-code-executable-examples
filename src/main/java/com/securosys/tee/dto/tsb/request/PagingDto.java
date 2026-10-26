/**
 * Copyright (c)2005 Securosys SA, authors: Tomasz Madej
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 **/
package com.securosys.tee.dto.tsb.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Metadata for enabling result paging")
public class PagingDto {

    @Min(value = 0)
    @Schema(description = "The number of the page of results to be returned.")
    private int pageNumber;

    @Min(value = 1)
    @Schema(description = "The number of results to be returned per page")
    private int pageSize;

    @NotNull
    @Schema(description = "Sort order of the results. Note: initial value for LAST_FETCHED_DATE is the creation date of the "
            + "task. LAST_FETCHED_DATE is updated every time a task is returned to the client using the appropriate REST "
            + "service.")
    private SortOrder sortOrder;

    public PagingDto() {
    } // NOSONAR

    public PagingDto(int pageNumber, int pageSize, SortOrder sortOrder) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortOrder = sortOrder;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public enum SortOrder {
        CREATION_DATE_ASC,
        CREATION_DATE_DESC,
        LAST_FETCHED_DATE_ASC,
        LAST_FETCHED_DATE_DESC
    }
}

