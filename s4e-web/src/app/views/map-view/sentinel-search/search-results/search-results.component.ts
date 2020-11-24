import {Component, EventEmitter, Input, Output} from '@angular/core';
import {SentinelSearchResult} from '../../state/sentinel-search/sentinel-search.model';

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
  @Output() close = new EventEmitter<void>();
  @Output() showDetails = new EventEmitter<SentinelSearchResult>();
  @Output() reload = new EventEmitter<void>();
  @Output() forbiddenAction = new EventEmitter<void>();


  interceptDownload($event: MouseEvent) {
    this.forbiddenAction.emit();
    $event.preventDefault();
  }
}
