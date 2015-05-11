
import com.miserablemind.api.consumer.tradeking.api.*;
import com.miserablemind.api.consumer.tradeking.api.domain.account.summary.AccountsSummary;
import com.miserablemind.api.consumer.tradeking.api.domain.market.StockQuote;
import com.miserablemind.api.consumer.tradeking.api.domain.member.UserProfile;
import com.miserablemind.api.consumer.tradeking.api.impl.TradeKingTemplate;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class TradeClient {
	
	public static void main(String[] args) {

		Properties credentials = getCredentials();
		TradeKingOperations tradeKingOperations = new TradeKingTemplate(
				credentials.getProperty("tradeking.consumerKey"),
				credentials.getProperty("tradeking.consumerSecret"),
				credentials.getProperty("tradeking.accessToken"),
				credentials.getProperty("tradeking.tokenSecret"));
		
		Timer timer = new Timer();
		StockJob mTask = new StockJob(tradeKingOperations, timer);
		timer.scheduleAtFixedRate(mTask, 0, 3000);
	}
	
	/**
	 * Example of pulling data about account, balances, etc..
	 *
	 * @param traderKingOperations api client implementation
	 */
	private static void printAccountsBalances(TradeKingOperations tradeKingOperations) {
		AccountOperations accountOperations = tradeKingOperations.getAccountOperations();

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
	private static void printCurrentUserEmail(TradeKingOperations tradeKingOperations) {
		APIOperations apiOperations = tradeKingOperations.getAPIOperations();
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
			InputStream inputStream = TradeClient.class.getClassLoader().getResourceAsStream(propFileName);
			credentials.load(inputStream);
		} catch (IOException exception) {
			System.out.println("property file '" + propFileName + "' not found in the classpath");
		}

		return credentials;
	}
}

class StockJob extends TimerTask{
	TradeKingOperations tradeKingOperations;
	Timer timer;
	private static String fileQuotePath = "/Users/gyupadhy/git/tradeking-api-example/src/main/resources/quotes.txt";

	public StockJob(TradeKingOperations tradeKingOperations, Timer timer){
		this.tradeKingOperations = tradeKingOperations;
		this.timer = timer;
	}
	
	@Override
	public void run() { 
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		double low, high, prevClosingPrice, targetPrice, ask;

		MarketOperations marketOperations = tradeKingOperations.getMarketOperations();
		StockQuote[] stocks = marketOperations.getQuoteForStocks(new String[]{getQuotes()});
		for (StockQuote stockQuote : stocks) {
			System.out.println("Last Price for: " + stockQuote.getSymbol() + ": $" + stockQuote.getAsk());
			low = stockQuote.getPriorDayLow();
			high = stockQuote.getPriorDayHigh();
			prevClosingPrice = stockQuote.getPriorDayClose();
			targetPrice = prevClosingPrice + ((high - low) / 2); 
			ask = stockQuote.getAsk();

			if(ask >= targetPrice){
				if(OrderPlacement.buy(stockQuote.getSymbol())){
					Mail.sendGmail(stockQuote.getSymbol().toUpperCase(), " bought = "+ask+"  "+dateFormat.format(cal.getTime()));
					timer.cancel();
					break;
				}
			}
		}
	}
	
	private static String getQuotes(){
		StringBuilder builder = new StringBuilder();
		List<String> list = FileUtils.readFile(new File(fileQuotePath));
		for (String quote : list){
			builder.append(quote+",");
		}
		return builder.deleteCharAt(builder.length() - 1).toString();
	}
}