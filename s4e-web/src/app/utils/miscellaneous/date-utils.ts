export function yyyymmdd(date: Date) {
  let month = '' + (date.getMonth() + 1);
  let day = '' + date.getDate();
  const year = date.getFullYear();

  if (month.length < 2) {
    month = '0' + month;
  }
  if (day.length < 2) {
    day = '0' + day;
  }

  return [year, month, day].join('-');
}

export function yyyymm(date: Date) {
  return yyyymmdd(date).substr(0, 7);
}

export function timezone(): string {
  return Intl.DateTimeFormat().resolvedOptions().timeZone
}
