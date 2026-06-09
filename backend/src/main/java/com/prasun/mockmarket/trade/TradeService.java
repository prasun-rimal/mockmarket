package com.prasun.mockmarket.trade;

import com.prasun.mockmarket.account.AccountRepository;
import com.prasun.mockmarket.common.ApiException;
import com.prasun.mockmarket.common.MoneyUtil;
import com.prasun.mockmarket.market.MarketService;
import com.prasun.mockmarket.portfolio.Holding;
import com.prasun.mockmarket.portfolio.HoldingRepository;
import com.prasun.mockmarket.user.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeService {
    private final AccountRepository accounts;
    private final HoldingRepository holdings;
    private final TransactionRepository transactions;
    private final MarketService marketService;

    public TradeService(AccountRepository accounts, HoldingRepository holdings, TransactionRepository transactions, MarketService marketService) {
        this.accounts = accounts;
        this.holdings = holdings;
        this.transactions = transactions;
        this.marketService = marketService;
    }

    @Transactional
    public TradeResponse buy(User user, TradeRequest request) {
        var symbol = request.symbol().trim().toUpperCase();
        var quantity = MoneyUtil.quantity(request.quantity());
        var quote = marketService.quote(symbol);
        var price = MoneyUtil.money(quote.price());
        var total = MoneyUtil.money(price.multiply(quantity));
        var account = accounts.findByUser(user).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found."));
        if (account.getCashBalance().compareTo(total) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough cash for this trade.");
        }
        account.setCashBalance(account.getCashBalance().subtract(total));
        var holding = holdings.findByUserAndSymbol(user, symbol)
                .map(existing -> updateAveragePrice(existing, quantity, price))
                .orElseGet(() -> new Holding(user, symbol, quantity, price));
        holdings.save(holding);
        transactions.save(new Transaction(user, TransactionType.BUY, symbol, quantity, price, total));
        return new TradeResponse("BUY", symbol, quantity, price, total, MoneyUtil.money(account.getCashBalance()), "Bought " + quantity.stripTrailingZeros().toPlainString() + " shares of " + symbol + ".");
    }

    @Transactional
    public TradeResponse sell(User user, TradeRequest request) {
        var symbol = request.symbol().trim().toUpperCase();
        var quantity = MoneyUtil.quantity(request.quantity());
        var holding = holdings.findByUserAndSymbol(user, symbol)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "You do not own this stock."));
        if (holding.getQuantity().compareTo(quantity) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You do not own enough shares to sell.");
        }
        var quote = marketService.quote(symbol);
        var price = MoneyUtil.money(quote.price());
        var total = MoneyUtil.money(price.multiply(quantity));
        var account = accounts.findByUser(user).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found."));
        account.setCashBalance(account.getCashBalance().add(total));
        var remaining = holding.getQuantity().subtract(quantity);
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            holdings.delete(holding);
        } else {
            holding.setQuantity(remaining);
            holdings.save(holding);
        }
        transactions.save(new Transaction(user, TransactionType.SELL, symbol, quantity, price, total));
        return new TradeResponse("SELL", symbol, quantity, price, total, MoneyUtil.money(account.getCashBalance()), "Sold " + quantity.stripTrailingZeros().toPlainString() + " shares of " + symbol + ".");
    }

    public List<TransactionResponse> history(User user) {
        return transactions.findByUserOrderByCreatedAtDesc(user).stream().map(TransactionResponse::from).toList();
    }

    private Holding updateAveragePrice(Holding holding, BigDecimal addedQuantity, BigDecimal buyPrice) {
        var oldCost = holding.getQuantity().multiply(holding.getAveragePrice());
        var addedCost = addedQuantity.multiply(buyPrice);
        var newQuantity = holding.getQuantity().add(addedQuantity);
        var newAverage = oldCost.add(addedCost).divide(newQuantity, 2, RoundingMode.HALF_UP);
        holding.setQuantity(newQuantity);
        holding.setAveragePrice(newAverage);
        return holding;
    }
}
