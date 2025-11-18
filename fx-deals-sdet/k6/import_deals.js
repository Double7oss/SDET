import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 10,
  duration: '15s',
};

function uuid() {
  // quick and dirty UUID v4
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

export default function () {
  const rows = [];
  for (let i = 0; i < 10; i++) {
    rows.push({
      dealId: uuid(),
      fromCurrency: 'USD',
      toCurrency: 'EUR',
      timestamp: new Date().toISOString(),
      amount: 1 + Math.floor(Math.random() * 100) / 100.0
    });
  }
  const res = http.post('http://localhost:8080/api/deals/import', JSON.stringify(rows), { headers: { 'Content-Type': 'application/json' } });
  check(res, { 'status was 200': (r) => r.status === 200 });
  sleep(1);
}
