package nereus.tasks;

import nereus.db.objects.MonsterSkill;

public class MonsterAttackCooldown implements Runnable {
   private MonsterSkill skill;

   public MonsterAttackCooldown(MonsterSkill skill) {
      super();
      this.skill = skill;
   }

   public void run() {
      this.skill.cooldown = false;
   }
}
