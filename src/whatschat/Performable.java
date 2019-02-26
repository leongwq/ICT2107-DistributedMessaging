package whatschat;

import java.util.List;

public interface Performable {
	public void appendToChat(String str);
	public void updateCurrentGroup();
	public void clearChat();
	public void updateChatWithHistory(List<String> conversations);
	public void enableChatButton();
	public void disableChatButton();
}
