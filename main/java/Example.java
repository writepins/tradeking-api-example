import com.miserablemind.api.consumer.tradeking.api.*;
import com.miserablemind.api.consumer.tradeking.api.domain.account.summary.AccountsSummary;
import com.miserablemind.api.consumer.tradeking.api.domain.market.StockQuote;
import com.miserablemind.api.consumer.tradeking.api.domain.market.StreamQuoteEvent;
import com.miserablemind.api.consumer.tradeking.api.domain.market.StreamTradeEvent;
import com.miserablemind.api.consumer.tradeking.api.domain.member.UserProfile;
import com.miserablemind.api.consumer.tradeking.api.impl.TradeKingTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class Example {

    public static void main(String[] args) {

        Properties credentials = getCredentials();
        TradeKingOperations traderKingOperations = new TradeKingTemplate(
                credentials.getProperty("tradeking.consumerKey"),
                credentials.getProperty("tradeking.consumerSecret"),
                credentials.getProperty("tradeking.accessToken"),
                credentials.getProperty("tradeking.tokenSecret"));

        printCurrentUserEmail(traderKingOperations);
        printAccountsBalances(traderKingOperations);
        printStockQuotes(traderKingOperations);
        printStreaming(traderKingOperations);

    }

    /**
     * Example of streaming quotes and trades
     * It registers a listener, in this case implementation is included as a subclass
     * To see this work you have to be on market time, or at least pre/post market
     *
     * @param traderKingOperations api client implementation
     */
    private static void printStreaming(TradeKingOperations traderKingOperations) {
        StreamingOperations streamingOperations = traderKingOperations.getStreamingOperations();
        ArrayList<StreamListener> listeners = new ArrayList<>();
        listeners.add(new StreamPrint());
        streamingOperations.quotesAndTradesStream(listeners, new String[]{"AAPL", "GOOG"});
    }


    /**
     * Example of pulling market quotes. Same sub-api can pull quotes for options and stocks.
     *
     * @param traderKingOperations api client implementation
     */
    private static void printStockQuotes(TradeKingOperations traderKingOperations) {
        MarketOperations marketOperations = traderKingOperations.getMarketOperations();
        StockQuote[] stocks = marketOperations.getQuoteForStocks(new String[]{"AAPL", "T"});
        for (StockQuote stockQuote : stocks) {
            System.out.println("Last Price for: " + stockQuote.getCompanyName() + ": $" + stockQuote.getLastPrice());
        }
    }

    /**
     * Example of pulling data about account, balances, etc..
     *
     * @param traderKingOperations api client implementation
     */
    private static void printAccountsBalances(TradeKingOperations traderKingOperations) {
        AccountOperations accountOperations = traderKingOperations.getAccountOperations();

        AccountsSummary[] accounts = accountOperations.getAccounts();

        for (AccountsSummary account : accounts) {
            System.out.println("Account value for: " + account.getAccountId() + ": $" + account.getAccountBalance().getAccountValue());
        }
    }

    /**
     * Example of pulling api connection user profile. Same sub-api pulls ApiStatus
     *
     * @param traderKingOperations api client implementation
     */
    private static void printCurrentUserEmail(TradeKingOperations traderKingOperations) {
        APIOperations apiOperations = traderKingOperations.getAPIOperations();
        UserProfile userProfile = apiOperations.getCurrentUser().getUserProfile();
        System.out.println("Email of current user: " + userProfile.getEmailAddress());
    }


    /**
     * Pull credentials from credential file
     *
     * @return api credentials
     */
    private static Properties getCredentials() {

        Properties credentials = new Properties();
        String propFileName = "credentials.properties";

        try {
            InputStream inputStream = Example.class.getClassLoader().getResourceAsStream(propFileName);
            credentials.load(inputStream);
        } catch (IOException exception) {
            System.out.println("property file '" + propFileName + "' not found in the classpath");
        }

        return credentials;
    }


    /**
     * Stream Listener class
     */
    static class StreamPrint implements StreamListener {

        static int TIMES_TO_RUN = 10;
        static int TIMES_RUN_ALREADY = 0;

        @Override
        public void onTrade(StreamTradeEvent tradeEvent) {

            if (TIMES_RUN_ALREADY >= TIMES_TO_RUN) return;

            System.out.println(tradeEvent.getSymbol() + " Trade Quantity: " + tradeEvent.getVolume() + ", last price: " + tradeEvent.getLastPrice());
            TIMES_RUN_ALREADY++;
        }

        @Override
        public void onQuote(StreamQuoteEvent quoteEvent) {

            if (TIMES_RUN_ALREADY >= TIMES_TO_RUN) return;

            System.out.println(quoteEvent.getSymbol() + " Ask price: " + quoteEvent.getAskPrice() + ", bid price: " + quoteEvent.getBidPrice());
            TIMES_RUN_ALREADY++;
        }
    }

}
