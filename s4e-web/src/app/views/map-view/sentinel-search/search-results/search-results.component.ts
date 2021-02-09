/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {
  Component,
  EventEmitter,
  Input,
  Output,
  Pipe,
  PipeTransform
} from '@angular/core';
import {SentinelSearchResult} from '../../state/sentinel-search/sentinel-search.model';

@Pipe({name: 'toPaginationArray'})
export class ToPaginationArrayPipe implements PipeTransform {
  transform(
    value: number,
    visiblePages: number,
    currentPage: number
  ): {index: number | null; label: string}[] {
    const pages = Array(value)
      .fill(0)
      .map((x, i) => ({index: i, label: `${i + 1}`}));

    if (value <= visiblePages) {
      return pages;
    }
    let start = Math.max(currentPage - Math.floor(visiblePages / 2), 0);
    let end = Math.min(start + visiblePages, value);

    if (end - start < visiblePages) {
      start = end - visiblePages;
    }

    return pages.slice(start, end);
  }
}

@Component({
  selector: 's4e-search-results',
  templateUrl: './search-results.component.html',
  styleUrls: ['./search-results.component.scss']
})
export class SearchResultsComponent {
  @Input() searchResults: SentinelSearchResult[] = [];
  @Input() isLoading: boolean = false;

  @Input() error: any | null = null;
  @Input() isUserLoggedIn: boolean = false;
  @Input() totalCount: number | null = null;
  @Input() resultPagesCount: number | null = null;
  @Input() currentPage: number | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() showDetails = new EventEmitter<SentinelSearchResult>();
  @Output() reload = new EventEmitter<void>();
  @Output() forbiddenAction = new EventEmitter<void>();
  @Output() changePage = new EventEmitter<number>();

  interceptDownload($event: MouseEvent) {
    this.forbiddenAction.emit();
    $event.preventDefault();
  }

  @Output() mouseenter = new EventEmitter<string | number>();
  @Output() mouseleave = new EventEmitter<void>();
}
