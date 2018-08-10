/**
 * Copyright(c) 2018
 * Ulord core developers
 */
package one.ulord.upaas.uxtoken.wallet.components;


import one.ulord.upaas.uxtoken.wallet.Application;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author haibo
 * @since 7/21/18
 */
public class PromptDialog extends Dialog<ButtonBar.ButtonData> {
    public enum PromptType{
        INFO,
        SUCCESS,
        FAIL
    };
    public PromptDialog(PromptType type, String title, String prompt, String content){
        setTitle(title);
        setHeaderText(prompt);

        ImageView logo = null;
        switch (type){
            case SUCCESS:
                logo = new ImageView(new Image(Application.class.getClassLoader()
                    .getResourceAsStream("graphics/paysuccess.png")));
            break;
        case FAIL:
                logo = new ImageView(new Image(Application.class.getClassLoader()
                .getResourceAsStream("graphics/payfail.png")));
            break;
        case INFO:
            logo = new ImageView(new Image(Application.class.getClassLoader()
                    .getResourceAsStream("graphics/info.png")));
        }
        logo.setFitHeight(30.0);
        logo.setFitWidth(30.0);
        getDialogPane().setGraphic(logo);

        ButtonType passwordButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(passwordButtonType);

        if (content != null) {
            setContentText(content);
        }
    }
}
