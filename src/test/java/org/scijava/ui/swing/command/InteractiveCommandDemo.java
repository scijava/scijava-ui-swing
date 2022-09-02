package org.scijava.ui.swing.command;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

@Plugin(type = Command.class, menuPath = "Test>Test Interactive Command")
public class InteractiveCommandDemo extends InteractiveCommand {

    @Parameter
    String a_string;

    @Parameter(choices={"A", "B", "C"})
    String another_string;

    @Override
    public void run() {
        // nothing
    }

    public static void main(String... args) throws Exception {

        Context context = new Context();
        context.service(UIService.class).showUI();
        context.service(CommandService.class).run(InteractiveCommandDemo.class, true);

    }
}
