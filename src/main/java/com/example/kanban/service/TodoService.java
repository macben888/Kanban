package com.example.kanban.service;

import com.example.kanban.model.Todo;

import static com.example.kanban.model.TodoStatus.*;

import com.example.kanban.repo.TodoRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
@Data
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepo;

    public List<Todo> getAllTodos() throws NoSuchElementException {
        List<Todo> result = todoRepo.findAll();
        if (!result.isEmpty()) return result;
        throw new NoSuchElementException("No Todos in Database yet");
    }

    public Todo getTodoById(Integer id) throws NoSuchElementException {
        Optional<Todo> todo = todoRepo.findById(id);
        if (todo.isPresent()) return todo.get();
        throw new NoSuchElementException(String.format("No Todo with id %d found", id));
    }

    public List<Todo> getTodosByText(String searchQuery) throws NoSuchElementException {
        return todoRepo.findByTitleOrDescription(searchQuery);
    }

    public Todo createTodo(String title, String description) throws IllegalArgumentException {
        checkIfTitleAndDescriptionGiven(title, description);
        return new Todo(title, description);
    }

    private Todo advanceTodoStatus(Todo todo) {
        if (todo.getStatus() == TODO) {
            todo.setStatus(DOING);
        } else {
            todo.setStatus(DONE);
        }
        return todoRepo.save(todo);
    }

    private Todo reverseTodoStatus(Todo todo) {
        if (todo.getStatus() == DONE) {
            todo.setStatus(DOING);
        } else {
            todo.setStatus(TODO);
        }
        return todoRepo.save(todo);
    }

    public List<Todo> updateTodos(List<Integer> ids, boolean advance) throws NoSuchElementException {
        return   ids.stream()
                .map(this::getTodoById)
                .map(advance ? this::advanceTodoStatus : this::reverseTodoStatus)
                .toList();
    }

    public void deleteTodos(List<Integer> ids) {
        todoRepo.deleteAll(ids.stream()
                .map(this::getTodoById)
                .toList());
    }

    public Todo updateTodoContent(Integer id, String title, String description) {
        checkIfTitleAndDescriptionGiven(title, description);
        Todo todoToUpdate = getTodoById(id);
        todoToUpdate.setTitle(title);
        todoToUpdate.setDescription(description);
        todoRepo.save(todoToUpdate);
        return todoToUpdate;
    }

    private void checkIfTitleAndDescriptionGiven(String title, String description) throws IllegalArgumentException {
        if (title.length() == 0 || description.length() == 0)
            throw new IllegalArgumentException("No title or description defined");
    }

}
