package nereus.tasks;

import it.gotoandplay.smartfoxserver.data.User;
import nereus.world.World;
import nereus.tasks.CancellableTask;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Restart implements Runnable, CancellableTask {
   private World world;
   private User user;
   private ScheduledFuture<?> running;

   public Restart(World world, User user) {
      this.world = world;
      this.user = user;
   }

   @Override
   public void run() {
      try {
         this.world.send(new String[]{"server", "Server restarting in 5 minutes."}, this.world.zone.getChannelList());
         Thread.sleep(TimeUnit.MINUTES.toMillis(4L));
         this.world.send(new String[]{"warning", "Server restarting in 1 minute."}, this.world.zone.getChannelList());
         Thread.sleep(TimeUnit.MINUTES.toMillis(1L));
         this.world.send(new String[]{"logoutWarning", "", "60"}, this.world.zone.getChannelList());
         this.world.shutdown();
         Thread.sleep(TimeUnit.SECONDS.toMillis(2L));
         Runtime.getRuntime().exec("cmd /c start start2.bat");
//         ExtensionHelper.instance().rebootServer();
         System.exit(0);
      } catch (IOException | InterruptedException iOException) {
         //
      }
   }

   @Override
   public void cancel() {
      this.running.cancel(true);
   }

   @Override
   public void setRunning(ScheduledFuture<?> running) {
      this.running = running;
      this.world.restart = this;
   }
}
