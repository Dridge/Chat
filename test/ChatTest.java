import org.junit.Before;
import org.junit.Test;
import richard.eldridge.chat.Chat;

import java.awt.*;

public class ChatTest {
    Chat chat;

    @Before
    public void setup() {
        chat = new Chat();
    }

    @Test
    public void testLoginAndClose() throws Exception {
        Robot robot = new Robot();
        chat.run();
        robot.mouseMove(10,10);
        //robot.keyPress(InputEvent.getMaskForButton(0));
    }
}
