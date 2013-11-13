package net.freifunk.autodeploy.selenium;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Abstraction layer around Selenium.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface Actor {

    /**
     * Use this to determine if the {@link HtmlUnitDriver} is used.
     */
    boolean usesHtmlUnitDriver();

    /**
     * All subsequent operations will be performed on the window.
     */
    void switchToWindow();

    /**
     * Load the specified url.
     */
    void navigateTo(String url);

    /**
     * All subsequent operations will be performed in the specified frame.
     */
    void selectFrame(String frameName);

    /**
     * Waits until the element specified by the selector is available.
     */
    void waitForElement(By by);

    /**
     * Waits until the element specified by the selector is clickable.
     */
    void waitForClickableElement(By by);

    /**
     * Waits until the page title contains the specified substring.
     */
    void waitForTitleContaining(String substring);

    /**
     * Gets the element specified by the selector. Will fail if the element is not available.
     */
    WebElement getElement(By by);

    /**
     * Gets the text contained in the element specified by the selector. Will fail if the element is not available.
     */
    String getTextOfElement(By by);

    /**
     * Clicks the element specified by the selector. Will fail if the element is not available.
     */
    void clickElement(By by);

    /**
     * Chooses the given file in the file chooser specified by the selector. Will fail if the element is not available.
     */
    void chooseFile(By by, File file);

    /**
     * Types the given text into the text input specified by the selector. Will fail if the element is not available.
     */
    void typeIntoTextInput(By by, String text);

    /**
     * Types the given password into the password input specified by the selector. Will fail if the element is not available.
     */
    void typeIntoPasswordInput(By by, String password);

    /**
     * Updates the given checkbox specified by the selector. If <code>checked</code> is <code>true</code> the checkbox will
     * be checked afterwards, otherwise it will be unchecked. Will fail if the element is not available.
     */
    void updateCheckbox(By by, boolean checked);

    /**
     * Executes the given Javascript code.
     */
    void executeJavascript(String js);

    /**
     * Waits until the element specified by the selector contains the given text.
     */
    void waitForElementContainingText(By by, String text);

    /**
     * Waits until the element specified by the selector contains the given text. Fails after the given timeout.
     */
    void waitForElementContainingText(By by, String text, int timeout, TimeUnit unit);

    /**
     * Confirms a prompt.
     */
    void confirmPrompt();
}
