package textspeech.thezaxis.speechtext;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private List<Chat> chatList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, genre, messageText;

        public MyViewHolder(View view) {
            super(view);
            /*title =view.findViewById(R.id.title);
            genre =view.findViewById(R.id.genre);
            year =view.findViewById(R.id.year)*/;
            messageText = view.findViewById(R.id.text_view);
        }
    }


    public ChatAdapter(List<Chat> chatList) {
        this.chatList = chatList;
    }

    @Override
    public int getItemViewType(int position) {
        String sender = chatList.get(position).getSender();
        if (sender.equals("me")){
            return 1;
        }
        else
            return 2;



        //return super.getItemViewType(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == 1){
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_sent, parent, false);
        }
        else{
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_received, parent, false);
        }


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.messageText.setText(chat.getMessage());
        /*Movie movie = chatList.get(position);
        holder.title.setText(movie.getTitle());
        holder.genre.setText(movie.getGenre());
        holder.year.setText(movie.getYear());*/
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}