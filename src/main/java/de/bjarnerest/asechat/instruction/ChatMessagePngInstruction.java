package de.bjarnerest.asechat.instruction;

import com.google.gson.stream.MalformedJsonException;
import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.PngImage;
import de.bjarnerest.asechat.model.Station;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatMessagePngInstruction extends BaseInstruction {

  protected final @NotNull PngImage pngImage;

  public ChatMessagePngInstruction(Station origin, @NotNull PngImage pngImage) {
    super(origin);
    this.pngImage = pngImage;
  }

  @SuppressWarnings("unused")
  @Contract("_, _ -> new")
  public static @NotNull ChatMessagePngInstruction fromString(String stringRepresentation, Station origin)
      throws InstructionInvalidException {

    String[] split = splitInstruction(stringRepresentation);
    if (!split[0].equals(InstructionNameHelper.getNameForInstruction(ChatMessagePngInstruction.class))) {
      throw new InstructionInvalidException();
    }

    // Try to parse payload
    try {
      Message newMessage = Message.fromJson(split[1]);
      return new ChatMessagePngInstruction(origin, PngImage.fromJson(split[1]));
    }
    catch (MalformedJsonException e) {
      throw new InstructionInvalidException();
    }

  }

  public PngImage getPngImage() {
    return pngImage;
  }

  @Override
  public String toString() {

    return InstructionNameHelper.getNameForInstruction(this.getClass())
        + "=" + this.pngImage.toJson();
  }

}
