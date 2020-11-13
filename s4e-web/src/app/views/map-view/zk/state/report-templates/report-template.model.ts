export interface ReportTemplate {
  id: number;
  caption: string;
  notes: string;
  overlaysIds: number[];

  createdAt?: string;
  productId?: number;
}
