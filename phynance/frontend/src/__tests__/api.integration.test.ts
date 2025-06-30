import { get, post } from '../api/apiClient';

jest.mock('../api/apiClient');

describe('API integration', () => {
  it('fetches market data', async () => {
    (get as jest.Mock).mockResolvedValueOnce([{ id: 1, symbol: 'AAPL' }]);
    const data = await get('/market-data?symbol=AAPL');
    expect(data).toEqual([{ id: 1, symbol: 'AAPL' }]);
  });

  it('posts analysis request', async () => {
    (post as jest.Mock).mockResolvedValueOnce({ result: 42 });
    const result = await post('/analysis', { foo: 'bar' });
    expect(result).toEqual({ result: 42 });
  });
});
