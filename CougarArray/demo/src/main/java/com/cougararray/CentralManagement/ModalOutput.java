package com.cougararray.CentralManagement;

public class ModalOutput {
    
    private String output;
    private boolean status;
    private boolean error;

    public ModalOutput(boolean status) {
        this.status = status;
    }

    public ModalOutput(boolean status, String output) {
        this.status = status;
        this.output = output;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void outputConsole() {
        //TODO! For Console Output
    }

    public void OutputJson() {
        //for Websocket
    }
}
