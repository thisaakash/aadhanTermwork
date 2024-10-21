package com.aakash.aadhan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EditStudentAdapter extends RecyclerView.Adapter<EditStudentAdapter.ViewHolder> {

    private List<Student> students;
    private OnEditClickListener listener;

    public EditStudentAdapter(List<Student> students, OnEditClickListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = students.get(position);
        holder.tvRollNumber.setText(student.getRollNumber());
        holder.tvName.setText(student.getName());
        holder.tvCompanyName.setText(student.getCompanyName());
        holder.tvTechnology.setText(student.getTechnology());
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(student));
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRollNumber, tvName, tvCompanyName, tvTechnology;
        Button btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRollNumber = itemView.findViewById(R.id.tvRollNumber);
            tvName = itemView.findViewById(R.id.tvName);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvTechnology = itemView.findViewById(R.id.tvTechnology);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    public interface OnEditClickListener {
        void onEditClick(Student student);
    }
}
