package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.AnsiColor;
import de.bjarnerest.asechat.model.Station;
import de.bjarnerest.asechat.model.User;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChangeUserInstruction extends BaseInstruction {

    private final @NotNull User user;

    public ChangeUserInstruction(Station origin, @NotNull User user) {
        super(origin);
        this.user = user;
    }

    @SuppressWarnings("unused")
    @Contract("_, _ -> new")
    public static @NotNull ChangeUserInstruction fromString(String stringRepresentation, Station origin)
            throws InstructionInvalidException {

        String[] split = splitInstruction(stringRepresentation);

        if (!split[0].equals(InstructionNameHelper.getNameForInstruction(ChangeUserInstruction.class))) {
            throw new InstructionInvalidException();
        }

        // Try to parse payload
        return new ChangeUserInstruction(origin, User.fromJson(split[1]));

    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {

        return InstructionNameHelper.getNameForInstruction(this.getClass())
                + "=" + this.user.toJson();
    }

}