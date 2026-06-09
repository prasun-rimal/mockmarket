import { Activity, ArrowDownRight, ArrowUpRight, BarChart3, Briefcase, Clock3, Eye, LogOut, RefreshCw, Search, ShieldCheck, Sparkles, Star, WalletCards } from 'lucide-react';
import type { ReactNode } from 'react';
import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Account, ApiError, Holding, Quote, SearchResult, Summary, Transaction, UserProfile, WatchlistItem, api } from './api';

type View = 'dashboard' | 'search' | 'portfolio' | 'watchlist' | 'history';
type Notice = { type: 'success' | 'error'; text: string } | null;

const money = (value = 0) => value.toLocaleString(undefined, { style: 'currency', currency: 'USD' });
const number = (value = 0) => Number(value).toLocaleString(undefined, { maximumFractionDigits: 4 });
const pct = (value = 0) => `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;
const STOCK_DETAIL_REFRESH_MS = 15_000;
const WATCHLIST_REFRESH_MS = 30_000;
const DASHBOARD_REFRESH_MS = 60_000;
const PORTFOLIO_REFRESH_MS = 60_000;

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('mockmarket_token'));
  const [user, setUser] = useState<UserProfile | null>(null);
  const [view, setView] = useState<View>('dashboard');
  const [notice, setNotice] = useState<Notice>(null);

  useEffect(() => {
    if (!token) return;
    api.me(token).then(setUser).catch(() => {
      localStorage.removeItem('mockmarket_token');
      setToken(null);
    });
  }, [token]);

  function onAuth(authToken: string, profile: UserProfile) {
    localStorage.setItem('mockmarket_token', authToken);
    setToken(authToken);
    setUser(profile);
  }

  function logout() {
    localStorage.removeItem('mockmarket_token');
    setToken(null);
    setUser(null);
  }

  if (!token || !user) return <AuthShell onAuth={onAuth} />;

  return (
    <div className="min-h-screen">
      <aside className="fixed inset-y-0 left-0 hidden w-64 border-r border-line/80 bg-ink/95 p-5 lg:block">
        <div className="mb-8 flex items-center gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-lg bg-mint text-ink"><BarChart3 size={22} /></div>
          <div>
            <p className="text-lg font-bold">MockMarket</p>
            <p className="text-xs text-slate-400">Paper trading terminal</p>
          </div>
        </div>
        <nav className="space-y-2">
          <NavButton icon={<Activity size={18} />} label="Dashboard" active={view === 'dashboard'} onClick={() => setView('dashboard')} />
          <NavButton icon={<Search size={18} />} label="Market Search" active={view === 'search'} onClick={() => setView('search')} />
          <NavButton icon={<Briefcase size={18} />} label="Portfolio" active={view === 'portfolio'} onClick={() => setView('portfolio')} />
          <NavButton icon={<Star size={18} />} label="Watchlist" active={view === 'watchlist'} onClick={() => setView('watchlist')} />
          <NavButton icon={<Clock3 size={18} />} label="Trade History" active={view === 'history'} onClick={() => setView('history')} />
        </nav>
        <button onClick={logout} className="absolute bottom-5 left-5 right-5 flex items-center gap-2 rounded-lg border border-line px-4 py-3 text-sm text-slate-300 hover:border-danger hover:text-danger">
          <LogOut size={17} /> Log out
        </button>
      </aside>

      <main className="lg:pl-64">
        <header className="sticky top-0 z-10 border-b border-line/70 bg-ink/85 px-4 py-4 backdrop-blur lg:px-8">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="text-sm text-slate-400">Welcome back, {user.name}</p>
              <h1 className="text-2xl font-bold tracking-tight">{titleFor(view)}</h1>
            </div>
            <div className="flex gap-2 overflow-x-auto lg:hidden">
              {(['dashboard', 'search', 'portfolio', 'watchlist', 'history'] as View[]).map(item => (
                <button key={item} onClick={() => setView(item)} className={`rounded-lg px-3 py-2 text-sm ${view === item ? 'bg-mint text-ink' : 'bg-panel text-slate-300'}`}>{titleFor(item).split(' ')[0]}</button>
              ))}
            </div>
          </div>
        </header>

        {notice && <div className={`mx-4 mt-5 rounded-lg px-4 py-3 text-sm lg:mx-8 ${notice.type === 'success' ? 'bg-mint/15 text-mint' : 'bg-danger/15 text-danger'}`}>{notice.text}</div>}

        <section className="p-4 lg:p-8">
          {view === 'dashboard' && <Dashboard token={token} setView={setView} />}
          {view === 'search' && <MarketSearch token={token} setNotice={setNotice} />}
          {view === 'portfolio' && <Portfolio token={token} setNotice={setNotice} />}
          {view === 'watchlist' && <Watchlist token={token} setNotice={setNotice} />}
          {view === 'history' && <History token={token} />}
        </section>
      </main>
    </div>
  );
}

function AuthShell({ onAuth }: { onAuth: (token: string, user: UserProfile) => void }) {
  const [mode, setMode] = useState<'login' | 'register'>('register');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      const response = mode === 'register'
        ? await api.register({ name, email, password })
        : await api.login({ email, password });
      onAuth(response.token, response.user);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not authenticate.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="grid min-h-screen grid-cols-1 lg:grid-cols-[1.1fr_0.9fr]">
      <section className="flex min-h-[48vh] flex-col justify-between p-6 lg:p-10">
        <div className="flex items-center gap-3">
          <div className="grid h-11 w-11 place-items-center rounded-lg bg-mint text-ink"><BarChart3 /></div>
          <p className="text-xl font-bold">MockMarket</p>
        </div>
        <div className="max-w-3xl py-10">
          <div className="mb-5 inline-flex items-center gap-2 rounded-full border border-mint/30 bg-mint/10 px-3 py-1 text-sm text-mint"><Sparkles size={15} /> $100,000 paper account</div>
          <h1 className="max-w-2xl text-5xl font-black leading-tight tracking-tight lg:text-7xl">Trade real market prices with fake money.</h1>
          <p className="mt-5 max-w-xl text-lg text-slate-300">A full-stack paper trading platform with JWT auth, PostgreSQL persistence, live Finnhub quotes, portfolio analytics, and a clean trading-dashboard interface.</p>
          <div className="mt-8 grid max-w-2xl gap-3 sm:grid-cols-3">
            <Feature icon={<ShieldCheck />} label="JWT secured" />
            <Feature icon={<WalletCards />} label="BigDecimal cash" />
            <Feature icon={<Eye />} label="Live watchlist" />
          </div>
        </div>
      </section>
      <section className="flex items-center justify-center border-t border-line bg-panel/55 p-6 lg:border-l lg:border-t-0">
        <form onSubmit={submit} className="glass w-full max-w-md rounded-lg p-6 shadow-glow">
          <div className="mb-6 flex rounded-lg bg-ink p-1">
            <button type="button" onClick={() => setMode('register')} className={`flex-1 rounded-md py-2 text-sm ${mode === 'register' ? 'bg-mint text-ink' : 'text-slate-300'}`}>Register</button>
            <button type="button" onClick={() => setMode('login')} className={`flex-1 rounded-md py-2 text-sm ${mode === 'login' ? 'bg-mint text-ink' : 'text-slate-300'}`}>Login</button>
          </div>
          {mode === 'register' && <label className="mb-3 block text-sm text-slate-300">Name<input className="input mt-1" value={name} onChange={e => setName(e.target.value)} required /></label>}
          <label className="mb-3 block text-sm text-slate-300">Email<input className="input mt-1" type="email" value={email} onChange={e => setEmail(e.target.value)} required /></label>
          <label className="mb-4 block text-sm text-slate-300">Password<input className="input mt-1" type="password" value={password} onChange={e => setPassword(e.target.value)} required /></label>
          {error && <p className="mb-4 rounded-lg bg-danger/15 px-3 py-2 text-sm text-danger">{error}</p>}
          <button disabled={loading} className="w-full rounded-lg bg-mint px-4 py-3 font-bold text-ink hover:bg-emerald-300 disabled:opacity-60">{loading ? 'Working...' : mode === 'register' ? 'Create paper account' : 'Enter dashboard'}</button>
          <p className="mt-4 text-center text-xs text-slate-500">Password needs 8+ chars, one uppercase letter, and one symbol.</p>
        </form>
      </section>
    </main>
  );
}

function Dashboard({ token, setView }: { token: string; setView: (view: View) => void }) {
  const { summary, account, history, watchlist, lastUpdated, refresh } = useDashboardData(token);
  const chart = useMemo(() => [
    { name: 'Start', value: 100000 },
    { name: 'Cash', value: summary?.cashBalance ?? 100000 },
    { name: 'Holdings', value: summary?.totalPortfolioValue ?? 100000 }
  ], [summary]);

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <Metric label="Cash Balance" value={money(summary?.cashBalance)} icon={<WalletCards />} />
        <Metric label="Holdings Value" value={money(summary?.holdingsValue)} icon={<Briefcase />} />
        <Metric label="Portfolio Value" value={money(summary?.totalPortfolioValue)} icon={<BarChart3 />} />
        <Metric label="Total Gain/Loss" value={`${money(summary?.totalGainLoss)} (${pct(summary?.totalGainLossPercent)})`} icon={(summary?.totalGainLoss ?? 0) >= 0 ? <ArrowUpRight /> : <ArrowDownRight />} tone={(summary?.totalGainLoss ?? 0) >= 0 ? 'gain' : 'loss'} />
      </div>
      <div className="grid gap-6 xl:grid-cols-[1.25fr_0.75fr]">
        <Panel title="Portfolio Curve" action={<div className="flex items-center gap-3"><LastUpdated at={lastUpdated} /><button onClick={refresh} className="rounded-lg border border-line p-2 text-slate-300 hover:text-mint" title="Refresh dashboard"><RefreshCw size={16} /></button><button onClick={() => setView('portfolio')} className="text-sm text-mint">View portfolio</button></div>}>
          <div className="h-72">
            <ResponsiveContainer>
              <AreaChart data={chart}>
                <defs><linearGradient id="value" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor="#39d98a" stopOpacity={0.35}/><stop offset="95%" stopColor="#39d98a" stopOpacity={0}/></linearGradient></defs>
                <CartesianGrid stroke="#263247" strokeDasharray="3 3" />
                <XAxis dataKey="name" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" domain={['dataMin - 500', 'dataMax + 500']} />
                <Tooltip formatter={(value) => money(Number(value))} contentStyle={{ background: '#101b2d', border: '1px solid #263247', borderRadius: 8 }} />
                <Area type="monotone" dataKey="value" stroke="#39d98a" fillOpacity={1} fill="url(#value)" strokeWidth={3} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Panel>
        <Panel title="Account">
          <div className="space-y-4">
            <p className="text-3xl font-black">{account?.username ?? 'Trader'}</p>
            <p className="text-slate-400">Starting balance {money(account?.startingBalance ?? 100000)}</p>
            <button onClick={() => setView('search')} className="w-full rounded-lg bg-mint px-4 py-3 font-bold text-ink">Find a stock</button>
          </div>
        </Panel>
      </div>
      <div className="grid gap-6 xl:grid-cols-3">
        <Panel title="Top Holdings">{summary?.topHoldings?.length ? <HoldingList holdings={summary.topHoldings} /> : <Empty text="No holdings yet." />}</Panel>
        <Panel title="Recent Trades">{history?.slice(0, 5).map(tx => <TransactionRow key={tx.id} tx={tx} />) ?? <Empty text="No trades yet." />}</Panel>
        <Panel title="Watchlist">{watchlist?.slice(0, 5).map(item => <QuoteRow key={item.symbol} item={item} />) ?? <Empty text="No watchlist symbols yet." />}</Panel>
      </div>
    </div>
  );
}

function MarketSearch({ token, setNotice }: { token: string; setNotice: (notice: Notice) => void }) {
  const [query, setQuery] = useState('apple');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [selected, setSelected] = useState('AAPL');
  const [holdings, setHoldings] = useState<Holding[]>([]);

  const refreshHoldings = () => api.holdings(token).then(setHoldings);
  const selectedHolding = holdings.find(holding => holding.symbol === selected);

  async function search(event?: FormEvent) {
    event?.preventDefault();
    const data = await api.search(query);
    setResults(data);
    if (data[0]) setSelected(data[0].symbol);
  }

  useEffect(() => { search(); refreshHoldings(); }, []);

  return (
    <div className="grid gap-6 xl:grid-cols-[0.8fr_1.2fr]">
      <Panel title="Market Search">
        <form onSubmit={search} className="flex gap-2">
          <input className="input" value={query} onChange={e => setQuery(e.target.value)} placeholder="Search Apple, Tesla, NVDA..." />
          <button className="rounded-lg bg-mint px-4 text-ink"><Search size={18} /></button>
        </form>
        <div className="mt-5 space-y-2">
          {results.map(result => (
            <button key={result.symbol} onClick={() => setSelected(result.symbol)} className={`w-full rounded-lg border px-4 py-3 text-left ${selected === result.symbol ? 'border-mint bg-mint/10' : 'border-line bg-ink/50'}`}>
              <p className="font-bold">{result.symbol}</p>
              <p className="text-sm text-slate-400">{result.description}</p>
            </button>
          ))}
        </div>
      </Panel>
      <StockDetail token={token} symbol={selected} ownedHolding={selectedHolding} setNotice={setNotice} onTradeComplete={refreshHoldings} />
    </div>
  );
}

function StockDetail({ token, symbol, ownedHolding, setNotice, onTradeComplete }: { token: string; symbol: string; ownedHolding?: Holding; setNotice: (notice: Notice) => void; onTradeComplete: () => void }) {
  const [quote, setQuote] = useState<Quote | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const ownedQuantity = ownedHolding?.quantity ?? 0;
  const canSell = ownedQuantity > 0 && quantity > 0 && quantity <= ownedQuantity;

  const refreshQuote = () => {
    if (!symbol) return;
    api.quote(symbol).then(data => {
      setQuote(data);
      setLastUpdated(new Date());
    });
  };

  useEffect(() => {
    refreshQuote();
    const interval = window.setInterval(refreshQuote, STOCK_DETAIL_REFRESH_MS);
    return () => window.clearInterval(interval);
  }, [symbol]);

  async function trade(type: 'buy' | 'sell') {
    try {
      const response = await api[type](token, { symbol, quantity });
      setNotice({ type: 'success', text: response.message });
      onTradeComplete();
      refreshQuote();
    } catch (err) {
      setNotice({ type: 'error', text: err instanceof ApiError ? err.message : 'Trade failed.' });
    }
  }

  async function addWatchlist() {
    await api.addWatchlist(token, symbol);
    setNotice({ type: 'success', text: `${symbol} added to watchlist.` });
  }

  if (!quote) return <Panel title="Stock Detail"><Empty text="Choose a symbol." /></Panel>;
  const up = quote.change >= 0;

  return (
    <Panel title={`${quote.symbol} Detail`} action={<div className="flex items-center gap-3"><LastUpdated at={lastUpdated} /><button onClick={refreshQuote} className="rounded-lg border border-line p-2 text-slate-300 hover:text-mint" title="Refresh quote"><RefreshCw size={16} /></button><button onClick={addWatchlist} className="rounded-lg border border-line p-2 text-mint" title="Add to watchlist"><Star size={17} /></button></div>}>
      <div className="grid gap-5 lg:grid-cols-[1fr_260px]">
        <div>
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div>
              <p className="text-5xl font-black">{money(quote.price)}</p>
              <p className={up ? 'text-mint' : 'text-danger'}>{up ? '+' : ''}{money(quote.change)} ({pct(quote.percentChange)}) today</p>
            </div>
          </div>
          <div className="mt-6 grid gap-3 sm:grid-cols-4">
            <Mini label="Open" value={money(quote.open)} />
            <Mini label="High" value={money(quote.high)} />
            <Mini label="Low" value={money(quote.low)} />
            <Mini label="Prev close" value={money(quote.previousClose)} />
          </div>
        </div>
        <div className="rounded-lg border border-line bg-ink/70 p-4">
          <label className="text-sm text-slate-300">Quantity<input className="input mt-1" type="number" min="0.0001" step="0.0001" value={quantity} onChange={e => setQuantity(Number(e.target.value))} /></label>
          <p className="mt-3 text-sm text-slate-400">Estimated value</p>
          <p className="text-2xl font-black">{money(quantity * quote.price)}</p>
          <p className="mt-3 text-xs text-slate-500">{ownedQuantity > 0 ? `You own ${number(ownedQuantity)} shares.` : 'You do not own this stock yet.'}</p>
          <div className={`mt-4 grid gap-2 ${ownedQuantity > 0 ? 'grid-cols-2' : 'grid-cols-1'}`}>
            <button onClick={() => trade('buy')} className="rounded-lg bg-mint px-4 py-3 font-bold text-ink">Buy</button>
            {ownedQuantity > 0 && <button onClick={() => trade('sell')} disabled={!canSell} className="rounded-lg bg-danger px-4 py-3 font-bold text-white disabled:cursor-not-allowed disabled:opacity-45">Sell</button>}
          </div>
        </div>
      </div>
    </Panel>
  );
}

function Portfolio({ token, setNotice }: { token: string; setNotice: (notice: Notice) => void }) {
  const [holdings, setHoldings] = useState<Holding[]>([]);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const refresh = () => api.holdings(token).then(data => {
    setHoldings(data);
    setLastUpdated(new Date());
  });

  useEffect(() => {
    refresh();
    const interval = window.setInterval(refresh, PORTFOLIO_REFRESH_MS);
    return () => window.clearInterval(interval);
  }, [token]);

  async function sell(symbol: string, quantity: number) {
    try {
      const response = await api.sell(token, { symbol, quantity });
      setNotice({ type: 'success', text: response.message });
      refresh();
    } catch (err) {
      setNotice({ type: 'error', text: err instanceof ApiError ? err.message : 'Sell order failed.' });
    }
  }

  return (
    <Panel title="Holdings" action={<div className="flex items-center gap-3"><LastUpdated at={lastUpdated} /><button onClick={refresh} className="rounded-lg border border-line p-2 text-slate-300 hover:text-mint" title="Refresh holdings"><RefreshCw size={16} /></button></div>}>
      {holdings.length ? <HoldingTable holdings={holdings} onSell={sell} /> : <Empty text="Your holdings will appear after your first buy." />}
    </Panel>
  );
}

function Watchlist({ token, setNotice }: { token: string; setNotice: (notice: Notice) => void }) {
  const [items, setItems] = useState<WatchlistItem[]>([]);
  const [symbol, setSymbol] = useState('AAPL');
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const refresh = () => api.watchlist(token).then(data => {
    setItems(data);
    setLastUpdated(new Date());
  });
  useEffect(() => {
    refresh();
    const interval = window.setInterval(refresh, WATCHLIST_REFRESH_MS);
    return () => window.clearInterval(interval);
  }, [token]);

  async function add(event: FormEvent) {
    event.preventDefault();
    await api.addWatchlist(token, symbol);
    setNotice({ type: 'success', text: `${symbol.toUpperCase()} added to watchlist.` });
    refresh();
  }

  async function remove(item: string) {
    await api.removeWatchlist(token, item);
    refresh();
  }

  return (
    <Panel title="Watchlist" action={<div className="flex items-center gap-3"><LastUpdated at={lastUpdated} /><button onClick={refresh} className="rounded-lg border border-line p-2 text-slate-300 hover:text-mint" title="Refresh watchlist"><RefreshCw size={16} /></button></div>}>
      <form onSubmit={add} className="mb-5 flex gap-2">
        <input className="input" value={symbol} onChange={e => setSymbol(e.target.value)} />
        <button className="rounded-lg bg-mint px-4 font-bold text-ink">Add</button>
      </form>
      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
        {items.map(item => <div key={item.symbol} className="rounded-lg border border-line bg-ink/60 p-4"><QuoteRow item={item} /><button onClick={() => remove(item.symbol)} className="mt-3 text-sm text-danger">Remove</button></div>)}
      </div>
    </Panel>
  );
}

function History({ token }: { token: string }) {
  const [history, setHistory] = useState<Transaction[]>([]);
  useEffect(() => { api.history(token).then(setHistory); }, [token]);
  return <Panel title="Trade History">{history.length ? history.map(tx => <TransactionRow key={tx.id} tx={tx} detailed />) : <Empty text="No trades recorded yet." />}</Panel>;
}

function useDashboardData(token: string) {
  const [summary, setSummary] = useState<Summary | null>(null);
  const [account, setAccount] = useState<Account | null>(null);
  const [history, setHistory] = useState<Transaction[]>([]);
  const [watchlist, setWatchlist] = useState<WatchlistItem[]>([]);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const refresh = () => {
    Promise.all([api.summary(token), api.account(token), api.history(token), api.watchlist(token)])
      .then(([s, a, h, w]) => {
        setSummary(s);
        setAccount(a);
        setHistory(h);
        setWatchlist(w);
        setLastUpdated(new Date());
      });
  };

  useEffect(() => {
    refresh();
    const interval = window.setInterval(refresh, DASHBOARD_REFRESH_MS);
    return () => window.clearInterval(interval);
  }, [token]);
  return { summary, account, history, watchlist, lastUpdated, refresh };
}

function NavButton({ icon, label, active, onClick }: { icon: ReactNode; label: string; active: boolean; onClick: () => void }) {
  return <button onClick={onClick} className={`flex w-full items-center gap-3 rounded-lg px-4 py-3 text-sm ${active ? 'bg-mint text-ink' : 'text-slate-300 hover:bg-panel'}`}>{icon}{label}</button>;
}

function Panel({ title, action, children }: { title: string; action?: ReactNode; children: ReactNode }) {
  return <section className="glass rounded-lg p-5"><div className="mb-5 flex items-center justify-between gap-4"><h2 className="text-lg font-bold">{title}</h2>{action}</div>{children}</section>;
}

function Metric({ label, value, icon, tone = 'neutral' }: { label: string; value: string; icon: ReactNode; tone?: 'gain' | 'loss' | 'neutral' }) {
  const color = tone === 'gain' ? 'text-mint' : tone === 'loss' ? 'text-danger' : 'text-white';
  return <div className="metric-card rounded-lg p-5"><div className="mb-4 flex items-center justify-between text-slate-400"><p className="text-sm">{label}</p>{icon}</div><p className={`text-2xl font-black ${color}`}>{value}</p></div>;
}

function Mini({ label, value }: { label: string; value: string }) {
  return <div className="rounded-lg border border-line bg-ink/60 p-3"><p className="text-xs text-slate-500">{label}</p><p className="font-bold">{value}</p></div>;
}

function Feature({ icon, label }: { icon: ReactNode; label: string }) {
  return <div className="rounded-lg border border-line bg-panel/70 p-4 text-sm text-slate-200">{icon}<p className="mt-2">{label}</p></div>;
}

function Empty({ text }: { text: string }) {
  return <p className="rounded-lg border border-dashed border-line p-6 text-center text-slate-400">{text}</p>;
}

function HoldingList({ holdings }: { holdings: Holding[] }) {
  return <div className="space-y-3">{holdings.map(item => <div key={item.symbol} className="flex items-center justify-between rounded-lg bg-ink/55 p-3"><div><p className="font-bold">{item.symbol}</p><p className="text-sm text-slate-400">{number(item.quantity)} shares</p></div><div className="text-right"><p>{money(item.marketValue)}</p><p className={item.gainLoss >= 0 ? 'text-sm text-mint' : 'text-sm text-danger'}>{pct(item.gainLossPercent)}</p></div></div>)}</div>;
}

function HoldingTable({ holdings, onSell }: { holdings: Holding[]; onSell: (symbol: string, quantity: number) => void }) {
  const [sellQuantities, setSellQuantities] = useState<Record<string, number>>({});

  function quantityFor(holding: Holding) {
    return sellQuantities[holding.symbol] ?? holding.quantity;
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[940px] text-left text-sm">
        <thead className="text-slate-400">
          <tr><th className="py-3">Symbol</th><th>Quantity</th><th>Avg Price</th><th>Current</th><th>Market Value</th><th>Gain/Loss</th><th>Sell</th></tr>
        </thead>
        <tbody>
          {holdings.map(h => {
            const sellQuantity = quantityFor(h);
            const validSell = sellQuantity > 0 && sellQuantity <= h.quantity;
            return (
              <tr key={h.symbol} className="border-t border-line">
                <td className="py-4 font-bold">{h.symbol}</td>
                <td>{number(h.quantity)}</td>
                <td>{money(h.averagePrice)}</td>
                <td>{money(h.currentPrice)}</td>
                <td>{money(h.marketValue)}</td>
                <td className={h.gainLoss >= 0 ? 'text-mint' : 'text-danger'}>{money(h.gainLoss)} ({pct(h.gainLossPercent)})</td>
                <td>
                  <div className="flex min-w-52 items-center gap-2">
                    <input className="input max-w-28 py-2" type="number" min="0.0001" max={h.quantity} step="0.0001" value={sellQuantity} onChange={event => setSellQuantities(current => ({ ...current, [h.symbol]: Number(event.target.value) }))} />
                    <button disabled={!validSell} onClick={() => onSell(h.symbol, sellQuantity)} className="rounded-lg bg-danger px-4 py-2 font-bold text-white disabled:cursor-not-allowed disabled:opacity-45">Sell</button>
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

function LastUpdated({ at }: { at: Date | null }) {
  return <span className="hidden text-xs text-slate-500 sm:inline">{at ? `Updated ${at.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit', second: '2-digit' })}` : 'Updating...'}</span>;
}

function TransactionRow({ tx, detailed = false }: { tx: Transaction; detailed?: boolean }) {
  const buy = tx.type === 'BUY';
  return <div className="mb-3 flex items-center justify-between rounded-lg border border-line bg-ink/50 p-3"><div><p className="font-bold"><span className={buy ? 'text-mint' : 'text-danger'}>{tx.type}</span> {tx.symbol}</p><p className="text-sm text-slate-400">{number(tx.quantity)} shares at {money(tx.price)}</p>{detailed && <p className="text-xs text-slate-500">{new Date(tx.createdAt).toLocaleString()}</p>}</div><p className="font-bold">{money(tx.totalAmount)}</p></div>;
}

function QuoteRow({ item }: { item: WatchlistItem }) {
  return <div className="flex items-center justify-between"><div><p className="font-bold">{item.symbol}</p><p className="text-sm text-slate-400">{money(item.price)}</p></div><p className={item.change >= 0 ? 'text-mint' : 'text-danger'}>{pct(item.percentChange)}</p></div>;
}

function titleFor(view: View) {
  return ({ dashboard: 'Dashboard', search: 'Market Search', portfolio: 'Portfolio', watchlist: 'Watchlist', history: 'Trade History' })[view];
}
