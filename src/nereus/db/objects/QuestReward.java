package nereus.db.objects;

public class QuestReward
{
   public int id;
   public int itemId;
   public int quantity;
   public double rate;
   public String type;

   public int getId()
   {
      return this.id;
   }

   public int getItemId()
   {
      return this.itemId;
   }

//   public QuestReward()
//   {
//      super();
//   }
}
