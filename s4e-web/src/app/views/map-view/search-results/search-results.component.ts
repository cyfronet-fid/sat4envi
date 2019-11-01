import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {S4eConfig} from '../../../utils/initializer/config.service';
import {SearchResult} from '../state/search-results/search-result.model';

@Component({
  selector: 's4e-search-results',
  templateUrl: './search-results.component.html',
  styleUrls: ['./search-results.component.scss']
})
export class SearchResultsComponent implements OnInit {

  @Input() public searchResults: SearchResult[] = [];
  @Input() public loading: boolean = true;
  @Output() public placeSelected = new EventEmitter<SearchResult>();

  constructor(private CONFIG: S4eConfig) {
  }

  ngOnInit() {
  }

}
