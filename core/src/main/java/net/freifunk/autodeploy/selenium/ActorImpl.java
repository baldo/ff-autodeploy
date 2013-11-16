package net.freifunk.autodeploy.selenium;

import static org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementSelectionStateToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleContains;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.inject.Inject;

/**
 * Default implementation of {@link Actor}.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class ActorImpl implements Actor {

    private static final class IsWebserverAvailable implements Predicate<WebDriver> {
        private final int _port;
        private final String _host;

        private IsWebserverAvailable(final int port, final String host) {
            _port = port;
            _host = host;
        }

        @Override
        public boolean apply(final WebDriver input) {
            LOG.trace("IsWebserverAvailable: {}, {}", _host, _port);

            try (final Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(_host, _port), 2000 /* 2 seconds */);
                final boolean connected = socket.isConnected();
                LOG.trace("IsWebserverAvailable done: {}, {} => {}", _host, _port, connected);
                return connected;
            } catch (final IOException e) {
                LOG.trace("IsWebserverAvailable done: {}, {} => false ({})", _host, _port, e.getClass().getSimpleName());
                return false;
            }
        }
    }

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
    public boolean usesHtmlUnitDriver() {
        return _webDriver instanceof HtmlUnitDriver;
    }

    @Override
    public void waitForWebserverBeingAvailable(final String host, final int port, final int timeout, final TimeUnit unit) {
        LOG.trace("waitForWebserverBeingAvailable: {}, {}, {}, {}", host, port, timeout, unit);
        _wait.withTimeout(timeout, unit).until(new IsWebserverAvailable(port, host));
        LOG.trace("waitForWebserverBeingAvailable done: {}, {}, {}, {}", host, port, timeout, unit);
    }

    @Override
    public void switchToWindow() {
        LOG.trace("switchToWindow: {}", _window);
        _webDriver.switchTo().window(_window);
        LOG.trace("switchToWindow done: {}", _window);
    }

    @Override
    public void navigateTo(final String url) {
        LOG.trace("navigateTo: {}", url);
        switchToWindow();
        _webDriver.navigate().to(url);
        LOG.trace("navigateTo done: {}", url);
    }

    @Override
    public void selectFrame(final String frameName) {
        LOG.trace("selectFrame: {}", frameName);
        switchToWindow();
        _wait.until(frameToBeAvailableAndSwitchToIt(frameName));
        LOG.trace("selectFrame done: {}", frameName);
    }

    @Override
    public void waitForElement(final By by) {
        LOG.trace("waitForElement: {}", by);
        _wait.until(presenceOfElementLocated(by));
        LOG.trace("waitForElement done: {}", by);
    }

    @Override
    public void waitForClickableElement(final By by) {
        LOG.trace("waitForClickableElement: {}", by);
        _wait.until(elementToBeClickable(by));
        LOG.trace("waitForClickableElement done: {}", by);
    }

    @Override
    public void waitForTitleContaining(final String substring) {
        LOG.trace("waitForTitleContaining: {}", substring);
        _wait.until(titleContains(substring));
        LOG.trace("waitForTitleContaining done: {}", substring);
    }

    @Override
    public WebElement getElement(final By by) {
        LOG.trace("getElement: {}", by);
        waitForElement(by);
        final WebElement element = _webDriver.findElement(by);
        LOG.trace("getElement done: {}", by);
        return element;
    }

    @Override
    public String getTextOfElement(final By by) {
        LOG.trace("getTextOfElement: {}", by);
        final WebElement element = getElement(by);
        final String text = element.getText();
        LOG.trace("getTextOfElement done: {} => {}", by, text);
        return text;
    }

    @Override
    public void clickElement(final By by) {
        LOG.trace("clickElement: {}", by);
        waitForClickableElement(by);
        final WebElement element = getElement(by);
        element.click();
        LOG.trace("clickElement done: {}", by);
    }

    @Override
    public void chooseFile(final By by, final File file) {
        LOG.trace("chooseFile: {}, {}", by, file);
        final WebElement element = getElement(by);
        Preconditions.checkState(
            "input".equals(element.getTagName()) && "file".equals(element.getAttribute("type")),
            "Element should be a file input: {}",
            element
        );
        element.sendKeys(file.getAbsoluteFile().getPath());
        LOG.trace("chooseFile done: {}, {}", by, file);
    }

    @Override
    public void typeIntoTextInput(final By by, final String text) {
        LOG.trace("typeIntoTextInput: {}, {}", by, text);
        final WebElement element = getElement(by);
        element.clear();
        Preconditions.checkState(
            "input".equals(element.getTagName()) && "text".equals(element.getAttribute("type")),
            "Element should be a text input: {}",
            element
        );
        element.sendKeys(text);
        LOG.trace("typeIntoTextInput done: {}, {}", by, text);
    }

    @Override
    public void typeIntoPasswordInput(final By by, final String password) {
        LOG.trace("typeIntoPasswordInput: {}, ********", by);
        final WebElement element = getElement(by);
        element.clear();
        Preconditions.checkState(
            "input".equals(element.getTagName()) && "password".equals(element.getAttribute("type")),
            "Element should be a password input: {}",
            element
        );
        element.sendKeys(password);
        LOG.trace("typeIntoPasswordInput done: {}, ********", by);
    }

    @Override
    public void updateCheckbox(final By by, final boolean checked) {
        LOG.trace("updateCheckbox: {}, {}", by, checked);
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
        LOG.trace("updateCheckbox done: {}, {}", by, checked);
    }

    @Override
    public void executeJavascript(final String js) {
        LOG.trace("executeJavascript: {}", js);
        final JavascriptExecutor javascriptExecutor = (JavascriptExecutor) _webDriver;
        javascriptExecutor.executeScript(js);
        LOG.trace("executeJavascript done: {}", js);
    }

    @Override
    public void waitForElementContainingText(final By by, final String text) {
        LOG.trace("waitForElementContainingText: {}, {}", by, text);
        _wait.until(textToBePresentInElement(by, text));
        LOG.trace("waitForElementContainingText done: {}, {}", by, text);
    }

    @Override
    public void waitForElementContainingText(final By by, final String text, final int timeout, final TimeUnit unit) {
        LOG.trace("waitForElementContainingText: {}, {}, {}, {}", by, text, timeout, unit);
        _wait.withTimeout(timeout, unit).until(textToBePresentInElement(by, text));
        LOG.trace("waitForElementContainingText done: {}, {}, {}, {}", by, text, timeout, unit);
    }

    @Override
    public void confirmPrompt() {
        LOG.trace("confirmPrompt");
        waitForAlert();
        final Alert prompt = _webDriver.switchTo().alert();
        prompt.accept();
        LOG.trace("confirmPrompt done");
    }

    private void waitForAlert() {
        LOG.trace("waitForAlert");
        _wait.until(alertIsPresent());
        LOG.trace("waitForAlert done");
    }
}
