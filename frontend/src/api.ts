export type UserProfile = { id: number; name: string; email: string };
export type AuthResponse = { token: string; user: UserProfile };
export type Quote = { symbol: string; price: number; change: number; percentChange: number; high: number; low: number; open: number; previousClose: number };
export type SearchResult = { symbol: string; description: string; type: string };
export type Account = { username: string; cashBalance: number; startingBalance: number };
export type Holding = { symbol: string; quantity: number; averagePrice: number; currentPrice: number; marketValue: number; gainLoss: number; gainLossPercent: number };
export type Summary = { cashBalance: number; holdingsValue: number; totalPortfolioValue: number; totalGainLoss: number; totalGainLossPercent: number; topHoldings: Holding[] };
export type Transaction = { id: number; type: 'BUY' | 'SELL'; symbol: string; quantity: number; price: number; totalAmount: number; createdAt: string };
export type WatchlistItem = { symbol: string; price: number; change: number; percentChange: number };
export type TradeResponse = { type: 'BUY' | 'SELL'; symbol: string; quantity: number; executionPrice: number; totalAmount: number; cashBalance: number; message: string };

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export class ApiError extends Error {
  constructor(message: string) {
    super(message);
  }
}

async function request<T>(path: string, token?: string | null, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set('Content-Type', 'application/json');
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const response = await fetch(`${API_URL}${path}`, { ...options, headers });
  if (!response.ok) {
    const payload = await response.json().catch(() => ({ message: 'Request failed.' }));
    throw new ApiError(payload.message ?? 'Request failed.');
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export const api = {
  wake: async () => {
    const response = await fetch(`${API_URL}/api/health`, { cache: 'no-store' });
    if (!response.ok) throw new ApiError('The server is still starting. Please try again in a few seconds.');
  },
  register: (body: { name: string; email: string; password: string }) => request<AuthResponse>('/api/auth/register', null, { method: 'POST', body: JSON.stringify(body) }),
  login: (body: { email: string; password: string }) => request<AuthResponse>('/api/auth/login', null, { method: 'POST', body: JSON.stringify(body) }),
  me: (token: string) => request<UserProfile>('/api/auth/me', token),
  quote: (symbol: string) => request<Quote>(`/api/market/quote/${encodeURIComponent(symbol)}`),
  search: (query: string) => request<SearchResult[]>(`/api/market/search?query=${encodeURIComponent(query)}`),
  account: (token: string) => request<Account>('/api/account', token),
  summary: (token: string) => request<Summary>('/api/portfolio/summary', token),
  holdings: (token: string) => request<Holding[]>('/api/portfolio/holdings', token),
  history: (token: string) => request<Transaction[]>('/api/trade/history', token),
  watchlist: (token: string) => request<WatchlistItem[]>('/api/watchlist', token),
  addWatchlist: (token: string, symbol: string) => request<WatchlistItem>(`/api/watchlist/${encodeURIComponent(symbol)}`, token, { method: 'POST' }),
  removeWatchlist: (token: string, symbol: string) => request<void>(`/api/watchlist/${encodeURIComponent(symbol)}`, token, { method: 'DELETE' }),
  buy: (token: string, body: { symbol: string; quantity: number }) => request<TradeResponse>('/api/trade/buy', token, { method: 'POST', body: JSON.stringify(body) }),
  sell: (token: string, body: { symbol: string; quantity: number }) => request<TradeResponse>('/api/trade/sell', token, { method: 'POST', body: JSON.stringify(body) })
};
