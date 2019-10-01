export interface Legend {
  type: 'gradient';
  url : string;
  leftDescription: {[key: number]: string};
  rightDescription: {[key: number]: string};
  metricTop: string;
  metricBottom: string
}

export interface ProductType {
  id: number;
  name: string;
  productIds: number[] | undefined;
  legend: Legend;
}
