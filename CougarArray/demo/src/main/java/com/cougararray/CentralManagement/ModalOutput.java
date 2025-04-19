package com.cougararray.CentralManagement;

import org.json.JSONObject;

public class ModalOutput {
    //STATUS = TRUE (GOOD) FALSE (BAD)
    //ERROR = TRUE (BAD) FALSE (GOOD)
    //oh mah gud this will be confusing...


    private String output;
    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    private boolean status;
    private boolean error;

    public ModalOutput(boolean status) {
        this.status = status;
    }

    public ModalOutput(boolean status, String output) {
        this.status = status;
        this.output = output;
    }

    public ModalOutput(boolean status, String output, boolean error){
        this.status=status;
        this.output=output;
        this.error=error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean getError() {
        return this.error;
    }

    public boolean getStatus(){
        return status;
    }

    public void outputConsole() {
        //TODO! For Console Output
    }

    public int outputStatusToInt() {
        if (error) return 2; //if error
        else if (!status) return 1; //if not successful
        else return 0; 
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("output", output);
        json.put("status", status);
        json.put("error", error);
        return json.toString();
    }
}
