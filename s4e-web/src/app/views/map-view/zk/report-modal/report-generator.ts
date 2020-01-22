import * as JsPDF from 'jspdf';
import {forkJoin, fromEvent, Observable, ReplaySubject} from 'rxjs';
import {delay, filter, map, switchMap, take, tap} from 'rxjs/operators';
import {HttpClient} from "@angular/common/http";
import {fromPromise} from "rxjs/internal-compatibility";

interface ReportImages {
  s4eLogo: null|{
    data: string;
    width: number;
    height: number;
  }
}

export class ReportGenerator {
  public readonly loading$: ReplaySubject<boolean> = new ReplaySubject<boolean>(1);
  public readonly working$: ReplaySubject<boolean> = new ReplaySubject<boolean>(1);

  private images: ReportImages = {
    s4eLogo: null
  };

  constructor(private http: HttpClient,
              private readonly image: string,
              private readonly imageWidth: number,
              private readonly imageHeight: number) {
    this.loading$.next(true);
    this.working$.next(false);
  }

  private loadImage(url: string, key: keyof ReportImages): Observable<any> {
    return this.http.get(url, {observe: 'response', responseType: 'blob'})
      .pipe(switchMap(
        img => {

          const reader = new FileReader();
          const r = fromEvent(reader, 'load').pipe(take(1), switchMap(
            () => {
              const image = new Image();
              const _r = fromEvent(image, 'load').pipe(tap(() => {
                this.images[key] = {
                  height: image.height,
                  width: image.width,
                  data: reader.result as string
                };
              }));

              image.src = reader.result as string;
              return _r;
            }
          ));

          reader.readAsDataURL(img.body);
          return r;
        }),
      );
  }

  private loadFonts(): Observable<any> {
    return fromPromise(import('./fonts/Ubuntu-Regular-normal').then((fontModule) => {
      fontModule.registerFont(JsPDF);
    }));
  }

  public generate(caption: string, notes: string): Observable<any> {
    // ignore this call if the generator is doing something
    return this.working$.pipe(
      take(1),
      filter(w => !w),
      tap(() => this.working$.next(true)),
      delay(0),
      tap(() => this.combineDocument(caption, notes)),
      delay(0),
      tap(() => this.working$.next(false))
    );
  }

  /**
   *
   * @return height of the added text box
   */
  private insertTextBox(doc: JsPDF, text: string, x: number, y: number, width: number, align: 'right'|'left'|'center' = 'left'): number {
    doc.text(text, x, y, {maxWidth: width, align: align});
    return 0;
  }

  private combineDocument(caption: string, notes: string) {
    caption = 'POLSKA - mapa sytuacyjna';
    notes = 'It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using \'Content here, content here\', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for \'lorem ipsum\' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).';

    let doc = new JsPDF({
      orientation: 'landscape',
      unit: 'mm',
      format: 'a4'
    });

    const A4Width = 297;
    const A4Height = 210;
    const DPM = (72.0 / 25.6); // density per mm
    const A4Ration = A4Width / A4Height;

    const printImageHeight = this.imageHeight / this.imageWidth * A4Width;

    const imageYOffset = (A4Height - printImageHeight) / 2.0;

    const margin = 10;
    const middleMargin = margin;
    const legendTextMargin = 40;

    const headerFontSize = 14.0;
    const noteFontSize = 10.0;
    const detailsFontSize = 9.0;
    const noteHeaderFontSize = 12.0;

    // noinspection JSSuspiciousNameCombination
    const legendX = A4Height;
    const maxTextWidth = A4Width - legendX - margin;

    doc.setFont('Ubuntu-Regular');
    doc.addImage(this.image, 'PNG', margin, margin, A4Height - margin - middleMargin, A4Height - 2 * margin);

    const logoWidthScaled = this.images.s4eLogo.width / 8.0;
    const logoHeightScaled = this.images.s4eLogo.height / 8.0;

    doc.addImage(this.images.s4eLogo.data, 'PNG', legendX + (maxTextWidth - logoWidthScaled) / 2.0, margin, logoWidthScaled, logoHeightScaled);

    doc.setFontSize(headerFontSize);
    // this.insertTextBox(doc, caption, )
    doc.text(caption, (A4Width - legendX - margin) / 2 + A4Height, legendTextMargin, {
      maxWidth: maxTextWidth,
      align: 'center'
    });

    doc.setFontSize(noteHeaderFontSize);
    // let textWidth = doc.getStringUnitWidth(notes) * noteFontSize / DPM;
    doc.text('Opis', legendX, legendTextMargin, {maxWidth: maxTextWidth});
    doc.setFontSize(noteFontSize);

    doc.text(notes, legendX, legendTextMargin + 5 + noteHeaderFontSize, {maxWidth: maxTextWidth});

    const detailsY = legendTextMargin + 60;
    doc.setFontSize(detailsFontSize);
    doc.text('Szczegóły', legendX, detailsY, {maxWidth: maxTextWidth});

    doc.text('Data wykonania zdjęcia', legendX, detailsY + 5);
    doc.text('Meteosat kanał 008', legendX, detailsY + 10);

    doc.text('Produkt', legendX, detailsY + 5);
    doc.text('12.01.2020 g. 14:00', legendX, detailsY + 10);

    doc.text('Data wykonania raportu', legendX, detailsY + 15);
    doc.text('15.01.2020 g. 13:50', legendX, detailsY + 20);

    doc.save(`RAPORT.${new Date().toISOString()}.pdf`);
  }

  loadAssets() {
    forkJoin([
      this.loadFonts(),
      this.loadImage('./assets/images/logo_s4e_color.png', 's4eLogo')
    ]).subscribe(
      () => this.loading$.next(false)
    );
  }
}
