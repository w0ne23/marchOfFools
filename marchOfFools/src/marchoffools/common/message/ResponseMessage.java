package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class ResponseMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private Object data;
    
    public ResponseMessage() {
        super();
    }
    
    public ResponseMessage(boolean success, String message) {
        super();
        this.success = success;
        this.message = message;
    }
    
    public ResponseMessage(boolean success, String message, Object data) {
        super();
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // Getter & Setter
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
}