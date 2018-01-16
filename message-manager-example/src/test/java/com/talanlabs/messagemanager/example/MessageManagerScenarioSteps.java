package com.talanlabs.messagemanager.example;

import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.model.DefaultErrorType;
import com.talanlabs.mm.server.IServer;
import com.talanlabs.mm.server.MM;
import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.mm.server.helper.FSHelper;
import com.talanlabs.mm.server.helper.TestUtils;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;

public class MessageManagerScenarioSteps {

    private IServer server;

    private Map<Serializable, MyImportFlux> messages;
    private MyMMImportAgent agent;

    @Given("^engine is created$")
    public void engineIsCreated() throws Throwable {
        server = new MM("example", FSHelper.getIntegFolder(), TestUtils.getErrorPath());
        messages = new HashMap<>();
    }

    @And("^dictionary is configured$")
    public void dictionaryIsConfigured() {
        MMDictionary dictionary = MMEngineAddon.getDictionary("example");
        dictionary.defineError(new DefaultErrorType("WARNING_ERROR", ErrorRecyclingKind.WARNING));
        dictionary.defineError(new DefaultErrorType("NOT_RECYCLABLE_ERROR", ErrorRecyclingKind.NOT_RECYCLABLE));
        dictionary.defineError(new DefaultErrorType("AUTOMATIC_ERROR", ErrorRecyclingKind.AUTOMATIC, 60));
        dictionary.defineError(new DefaultErrorType("MANUAL_ERROR", ErrorRecyclingKind.MANUAL));
    }

    @And("^agent is created and registered$")
    public void agentIsCreatedAndRegistered() {
        agent = new MyMMImportAgent(messages);
        agent.register("example", 5, 200, new File(FSHelper.getIntegFolder()));
    }

    @And("^engine is initialized$")
    public void engineIsInitialized() {
        server.start();
    }

    @When("^a valid file is received$")
    public void aValidFileIsReceived() throws IOException, InterruptedException {
        String content = "ACCEPT";
        injectFile(content);
    }

    @When("^a file with a warning error is received$")
    public void aFileWithAWarningErrorIsReceived() throws IOException, InterruptedException {
        injectFile("WARNING_ERROR");
    }

    @Then("^the file should be accepted$")
    public void theFileShouldBeAccepted() {
        check(MessageStatus.PROCESSED, 1);
    }

    @Then("^the file should be accepted with warnings$")
    public void theFileShouldBeAcceptedWithWarnings() {
        check(MessageStatus.PROCESSED, 2);
    }

    @When("^an invalid file with an automatic error is received$")
    public void anInvalidFileWithAnAutomaticErrorIsReceived() throws IOException, InterruptedException {
        injectFile("AUTOMATIC_ERROR");
    }

    @Then("^the file should be in recycling automatically state$")
    public void theFileShouldBeInRecyclingAutomaticallyState() {
        check(MessageStatus.TO_RECYCLE_AUTOMATICALLY, 2);
    }

    @When("^an invalid file with a manual error is received$")
    public void anInvalidFileWithAManualErrorIsReceived() throws IOException, InterruptedException {
        injectFile("MANUAL_ERROR");
    }

    @Then("^the file should be in recycling manually state$")
    public void theFileShouldBeInRecyclingManuallyState() {
        check(MessageStatus.TO_RECYCLE_MANUALLY, 2);
    }

    @When("^an invalid file with a reject definitely is received$")
    public void anInvalidFileWithARejectDefinitelyIsReceived() throws IOException, InterruptedException {
        injectFile("FINAL_ERROR");
    }

    @Then("^the file should be rejected$")
    public void theFileShouldBeRejected() {
        check(MessageStatus.REJECTED, 2);
    }

    private void injectFile(String content) throws IOException, InterruptedException {
        File tmpFile = File.createTempFile("tmp", "message");
        try (FileWriter fw = new FileWriter(tmpFile)) {
            IOUtils.write(content, fw);
        }
        Assertions.assertThat(tmpFile.renameTo(new File(agent.getWorkDir(), "message-" + UUID.randomUUID()))).isTrue();

        TestUtils.sleep(500);
    }

    private void check(MessageStatus expected, int expectedErrors) {
        Assertions.assertThat(messages).hasSize(1);
        Serializable id = messages.keySet().iterator().next();
        MyImportFlux myImportFlux = messages.values().iterator().next();
        Assertions.assertThat(myImportFlux.getMessageStatus()).isEqualTo(expected);
        Assertions.assertThat(myImportFlux.getId()).isEqualTo(id);
        Assertions.assertThat(myImportFlux.getErrors()).hasSize(expectedErrors);
    }

    @Then("^shutdown the engine$")
    public void shutdownTheEngine() {
        server.stop();
    }
}
