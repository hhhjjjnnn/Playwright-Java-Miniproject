package pages;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

import java.util.List;

public class ShoppingCartPage {
    private final Page page;
    private final String expectedPageTitle = "Shopping Cart";
    private final String shoppingCartHeadingLocator = "h1:has-text('Shopping Cart')";
    private final String continueShoppingButtonLocator = "button[name='continueshopping']";
    private final String estimateShippingButtonLocator = "#open-estimate-shipping-popup";
    private final String productSubtotalLocator = ".product-subtotal";
    private final String orderTotalSumLocator = "//tr[@class='order-total']//strong";
    private final String deleteItemFromCartButtonLocator = ".remove-btn";
    private final String emptyCartText = "Your Shopping Cart is empty!";
    private final String ajaxLoadingBlockWindowLocator = ".ajax-loading-block-window";

    public ShoppingCartPage(Page page) {
        this.page = page;
    }

    public boolean isShoppingCartPageDisplayed() {
        boolean isPageTitleCorrect = verifyShoppingCartPageTitle();
        boolean isShoppingCartHeadingDisplayed = verifyShoppingCartHeading();
        return isPageTitleCorrect && isShoppingCartHeadingDisplayed;
    }

    public boolean verifyShoppingCartPageTitle(){
        String pageTitle = page.title();
        return pageTitle.contains(expectedPageTitle);
    }

    private boolean verifyShoppingCartHeading() {
        page.waitForSelector(shoppingCartHeadingLocator);
        return page.isVisible(shoppingCartHeadingLocator);
    }

    public boolean isContinueShoppingButtonDisplayed() {
        return page.isVisible(continueShoppingButtonLocator);
    }
    public boolean isEstimateShippingButtonDisplayed() {
        return page.isVisible(estimateShippingButtonLocator);
    }

    public double calculateTotalPriceOfProducts () {
        List<ElementHandle> productSubtotalElements = page.querySelectorAll(productSubtotalLocator);
        double totalPrice = 0.0;
        for (ElementHandle productSubtotalElement : productSubtotalElements) {
            String priceString = productSubtotalElement.innerText().replaceAll("[^\\d.]", "");
            double price = Double.parseDouble(priceString);
            totalPrice += price;
        }
        return totalPrice;
    }

    public double getTotalSumDisplayed() {
        String totalPriceString = page.innerText(orderTotalSumLocator).replaceAll("[^\\d.]", "");
        return Double.parseDouble(totalPriceString);
    }

    public int getNumberOfItemsInCart() {
        try {
            waitForPageLoaded();
            page.waitForSelector(deleteItemFromCartButtonLocator);
            return page.querySelectorAll(deleteItemFromCartButtonLocator).size();
        } catch (PlaywrightException e) {
            System.out.println("Delete item button not found. Returning 0.");
            return 0;
        }
    }

    public void clickNthDeleteButton(int itemNumber) {
        List<ElementHandle> deleteButtons = page.querySelectorAll(deleteItemFromCartButtonLocator);
        if (itemNumber > 0 && itemNumber <= deleteButtons.size()) {
            deleteButtons.get(itemNumber - 1).click();
            waitForPageLoaded();
        } else {
            throw new IllegalArgumentException("Invalid value for item number: " + itemNumber);
        }
    }

    public boolean isShoppingCartEmpty(){
        return page.textContent("body").contains(emptyCartText);
    }

    public void waitForPageLoaded() {
        // ensures ajaxLoadingBlockWindowLocator is not displayed
        page.waitForFunction(
                String.format(
                        "document.querySelector('%s').style.display === 'none'",
                        ajaxLoadingBlockWindowLocator
                )
        );
    }
}
