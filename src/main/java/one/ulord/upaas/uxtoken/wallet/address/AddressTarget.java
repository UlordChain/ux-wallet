package one.ulord.upaas.uxtoken.wallet.address;

import javafx.beans.property.SimpleStringProperty;

public class AddressTarget {
    final SimpleStringProperty address;
    final SimpleStringProperty amount;

    private SimpleStringProperty submitDate;
    private SimpleStringProperty tradeResult;

    public AddressTarget(String address, String amount) {
        this.address = new SimpleStringProperty(address);
        this.amount = new SimpleStringProperty(amount);
        this.submitDate = new SimpleStringProperty("");
        this.tradeResult = new SimpleStringProperty("");
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getAmount() {
        return amount.get();
    }

    public SimpleStringProperty amountProperty() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount.set(amount);
    }

    public String getSubmitDate() {
        return submitDate.get();
    }

    public SimpleStringProperty submitDateProperty() {
        return submitDate;
    }

    public void setSubmitDate(String submitDate) {
        this.submitDate.set(submitDate);
    }

    public String getTradeResult() {
        return tradeResult.get();
    }

    public SimpleStringProperty tradeResultProperty() {
        return tradeResult;
    }

    public void setTradeResult(String tradeResult) {
        this.tradeResult.set(tradeResult);
    }
}

