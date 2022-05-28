package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.AnsiColor;
import de.bjarnerest.asechat.model.Station;
import de.bjarnerest.asechat.model.User;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChangeUserInstruction extends BaseInstruction {

    private final @Nullable User user;

    public ChangeUserInstruction(Station origin, User user) {
        super(origin);
        this.user = user;
    }

    public ChangeUserInstruction(Station origin) {
        this(origin, null);
    }

    @SuppressWarnings("unused")
    @Contract("_, _ -> new")
    public static @NotNull ChangeUserInstruction fromString(String stringRepresentation, Station origin)
            throws InstructionInvalidException {

        if(!stringRepresentation.contains("=")) {
            return new ChangeUserInstruction(origin);
        }

        String[] split = splitInstruction(stringRepresentation);

        if (!split[0].equals(InstructionNameHelper.getNameForInstruction(ChangeUserInstruction.class))) {
            throw new InstructionInvalidException();
        }

        // Try to parse payload
        return new ChangeUserInstruction(origin, User.fromJson(split[1]));

    }

    public @Nullable User getUser() {
        return user;
    }

    @Override
    public String toString() {

        if(user == null) {
            return InstructionNameHelper.getNameForInstruction(this.getClass());
        }

        return InstructionNameHelper.getNameForInstruction(this.getClass())
                + "=" + this.user.toJson();
    }

    public ChangeUserInstruction copywithStation(Station origin) {
        return new ChangeUserInstruction(origin, user);
    }

}