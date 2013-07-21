package net.freifunk.autodeploy.selenium;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementSelectionStateToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleContains;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Default implementation of {@link Actor}.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class ActorImpl implements Actor {

    private static final Logger LOG = LoggerFactory.getLogger(ActorImpl.class);

    private final WebDriver _webDriver;
    private final WebDriverWait _wait;
    private final String _window;

    @Inject
    public ActorImpl(
        final WebDriver webDriver,
        final WebDriverWait wait
    ) {
        _webDriver = webDriver;
        _wait = wait;

        _window = _webDriver.getWindowHandle();
    }

    @Override
    public void switchToWindow() {
        LOG.debug("switchToWindow: {}", _window);
        _webDriver.switchTo().window(_window);
        LOG.debug("switchToWindow done: {}", _window);
    }

    @Override
    public void navigateTo(final String url) {
        LOG.debug("navigateTo: {}", url);
        switchToWindow();
        _webDriver.navigate().to(url);
        LOG.debug("navigateTo done: {}", url);
    }

    @Override
    public void selectFrame(final String frameName) {
        LOG.debug("selectFrame: {}", frameName);
        switchToWindow();
        _wait.until(frameToBeAvailableAndSwitchToIt(frameName));
        LOG.debug("selectFrame done: {}", frameName);
    }

    @Override
    public void waitForElement(final By by) {
        LOG.debug("waitForElement: {}", by);
        _wait.until(presenceOfElementLocated(by));
        LOG.debug("waitForElement done: {}", by);
    }

    @Override
    public void waitForClickableElement(final By by) {
        LOG.debug("waitForClickableElement: {}", by);
        _wait.until(elementToBeClickable(by));
        LOG.debug("waitForClickableElement done: {}", by);
    }

    @Override
    public void waitForTitleContaining(final String substring) {
        LOG.debug("waitForTitleContaining: {}", substring);
        _wait.until(titleContains(substring));
        LOG.debug("waitForTitleContaining done: {}", substring);
    }

    @Override
    public WebElement getElement(final By by) {
        LOG.debug("getElement: {}", by);
        waitForElement(by);
        final WebElement element = _webDriver.findElement(by);
        LOG.debug("getElement done: {}", by);
        return element;
    }

    @Override
    public String getTextOfElement(final By by) {
        LOG.debug("getTextOfElement: {}", by);
        final WebElement element = getElement(by);
        final String text = element.getText();
        LOG.debug("getTextOfElement done: {} => {}", by, text);
        return text;
    }

    @Override
    public void clickElement(final By by) {
        LOG.debug("clickElement: {}", by);
        waitForClickableElement(by);
        final WebElement element = getElement(by);
        element.click();
        LOG.debug("clickElement done: {}", by);
    }

    @Override
    public void chooseFile(final By by, final File file) {
        LOG.debug("chooseFile: {}, {}", by, file);
        final WebElement element = getElement(by);
        Preconditions.checkState(
            "input".equals(element.getTagName()) && "file".equals(element.getAttribute("type")),
            "Element should be a file input: {}",
            element
        );
        element.sendKeys(file.getAbsoluteFile().getPath());
        LOG.debug("chooseFile done: {}, {}", by, file);
    }

    @Override
    public void typeIntoTextInput(final By by, final String text) {
        LOG.debug("typeIntoTextInput: {}, {}", by, text);
        final WebElement element = getElement(by);
        element.clear();
        Preconditions.checkState(
            "input".equals(element.getTagName()) && "text".equals(element.getAttribute("type")),
            "Element should be a text input: {}",
            element
        );
        element.sendKeys(text);
        LOG.debug("typeIntoTextInput done: {}, {}", by, text);
    }

    @Override
    public void typeIntoPasswordInput(final By by, final String password) {
        LOG.debug("typeIntoPasswordInput: {}, ********", by);
        final WebElement element = getElement(by);
        element.clear();
        Preconditions.checkState(
            "input".equals(element.getTagName()) && "password".equals(element.getAttribute("type")),
            "Element should be a password input: {}",
            element
        );
        element.sendKeys(password);
        LOG.debug("typeIntoPasswordInput done: {}, ********", by);
    }

    @Override
    public void updateCheckbox(final By by, final boolean checked) {
        LOG.debug("updateCheckbox: {}, {}", by, checked);
        waitForClickableElement(by);
        final WebElement checkbox = getElement(by);
        Preconditions.checkState(
            "input".equals(checkbox.getTagName()) && "checkbox".equals(checkbox.getAttribute("type")),
            "Element should be a checkbox: {}",
            checkbox
        );
        if (checkbox.isSelected() != checked) {
            checkbox.click();
        }
        _wait.until(elementSelectionStateToBe(by, checked));
        LOG.debug("updateCheckbox done: {}, {}", by, checked);
    }

    @Override
    public void executeJavascript(final String js) {
        LOG.debug("executeJavascript: {}", js);
        final JavascriptExecutor javascriptExecutor = (JavascriptExecutor) _webDriver;
        javascriptExecutor.executeScript(js);
        LOG.debug("executeJavascript done: {}", js);
    }

    @Override
    public void waitForElementContainingText(final By by, final String text) {
        LOG.debug("waitForElementContainingText: {}, {}", by, text);
        _wait.until(textToBePresentInElement(by, text));
        LOG.debug("waitForElementContainingText done: {}, {}", by, text);
    }

    @Override
    public void waitForElementContainingText(final By by, final String text, final int timeout, final TimeUnit unit) {
        LOG.debug("waitForElementContainingText: {}, {}, {}, {}", by, text, timeout, unit);
        _wait.withTimeout(timeout, unit).until(textToBePresentInElement(by, text));
        LOG.debug("waitForElementContainingText done: {}, {}, {}, {}", by, text, timeout, unit);
    }
}
