export interface ReportTemplate {
  uuid: string;
  caption: string;
  notes: string;
  overlayIds: number[];

  createdAt?: string;
  productId?: number;
}
