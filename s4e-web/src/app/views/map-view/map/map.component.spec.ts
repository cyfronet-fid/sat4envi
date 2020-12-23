/*
 * Copyright 2020 ACC Cyfronet AGH
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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MapComponent} from './map.component';
import {ShareModule} from '../../../common/share.module';
import {ReplaySubject} from 'rxjs';
import {take} from 'rxjs/operators';
import {RouterTestingModule} from '@angular/router/testing';
import {RemoteConfigurationTestingProvider} from '../../../app.configuration.spec';


describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [RemoteConfigurationTestingProvider],
      imports: [
        ShareModule,
        RouterTestingModule
      ],
      declarations: [MapComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    component.ngOnInit();
  });

  it('should getMapData generate image', (done) => {
    const spy = spyOn((component as any).map, 'renderSync');

    spyOn((component as any).map, 'once').and.callFake((event, callback) => {
      expect(event).toBe('rendercomplete');
      callback();
    });

    component.getMapData().pipe(take(1)).subscribe(data => {
      expect(data).toEqual({height: 150, width: 300, image: 'data:image/png;base64,00', 'pointResolution': 152.87405656527181});
      done();
    });
    expect(spy).toHaveBeenCalled();
  });

  it('should downloadImage', () => {
    const image = 'data:image/png;base64,00';
    const mapImage = new ReplaySubject(1);
    const spy = spyOn(component.linkDownload.nativeElement, 'click');
    spyOn(component, 'getMapData').and.returnValue(mapImage);
    component.downloadMap();
    mapImage.next({image: image, width: 1, height: 1});
    mapImage.complete();

    expect(component.linkDownload.nativeElement.getAttribute('download')).toContain('SNAPSHOT');
    expect(component.linkDownload.nativeElement.getAttribute('href')).toEqual(image);
    expect(spy).toHaveBeenCalled();
  });

  it('should emit event with correct values', () => {
    spyOn(component.viewChanged, 'emit');

    class MockView {
      public getCenter = () => [1, 2];
      public getZoom = () => 9;
    }

    class MockMap {
      public getView = () => new MockView();
    }

    component.onMoveEnd({map: new MockMap()});

    const expectedView = {
      centerCoordinates: [1, 2],
      zoomLevel: 9
    };
    expect(component.viewChanged.emit).toHaveBeenCalledWith(expectedView);
  });
});
