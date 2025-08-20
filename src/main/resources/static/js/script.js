class TodoApp {
    constructor() {
        this.todos = [];
        this.currentFilter = 'all';
        this.isEditing = false;
        
        this.initializeElements();
        this.attachEventListeners();
        this.loadTodos();
    }

    initializeElements() {
        // フォーム要素
        this.todoForm = document.getElementById('todo-form');
        
        // 入力要素
        this.todoInput = document.getElementById('todo-input');
        
        // 表示要素
        this.todoList = document.getElementById('todo-list');
        this.todoCount = document.getElementById('todo-count');
        this.loadingDiv = document.getElementById('loading');
        this.emptyState = document.getElementById('empty-state');
        
        // ボタン要素
        this.filterBtns = document.querySelectorAll('.filter-btn');
        
        // テンプレート
        this.todoItemTemplate = document.getElementById('todo-item-template');
    }

    attachEventListeners() {
        // Todoフォーム
        this.todoForm.addEventListener('submit', (e) => this.handleAddTodo(e));
        
        // フィルターボタン
        this.filterBtns.forEach(btn => {
            btn.addEventListener('click', (e) => this.handleFilterChange(e));
        });
    }

    // Todo追加処理
    async handleAddTodo(e) {
        e.preventDefault();
        
        const text = this.todoInput.value.trim();
        if (!text) return;

        try {
            const response = await this.apiCall('/api/todos', {
                text: text
            }, 'POST');

            if (response.success) {
                this.todos.unshift(response.todo);
                this.todoInput.value = '';
                this.updateTodoDisplay();
            } else {
                alert('Todo追加に失敗しました: ' + (response.message || ''));
            }
        } catch (error) {
            console.error('Todo追加エラー:', error);
            alert('Todo追加中にエラーが発生しました');
        }
    }

    // Todo読み込み
    async loadTodos() {
        this.showLoading(true);
        
        try {
            const response = await this.apiCall('/api/todos', {}, 'GET');
            
            if (response.success) {
                this.todos = response.todos || [];
                this.updateTodoDisplay();
            } else {
                console.error('Todo読み込み失敗:', response.message);
            }
        } catch (error) {
            console.error('Todo読み込みエラー:', error);
        } finally {
            this.showLoading(false);
        }
    }

    // Todo完了切り替え
    async toggleTodo(id) {
        const todo = this.todos.find(t => t.id === id);
        if (!todo) return;

        try {
            const response = await this.apiCall(`/api/todos/${id}`, {
                completed: !todo.completed
            }, 'PUT');

            if (response.success) {
                todo.completed = !todo.completed;
                this.updateTodoDisplay();
            }
        } catch (error) {
            console.error('Todo更新エラー:', error);
        }
    }

    // Todo編集開始
    startEditTodo(id) {
        if (this.isEditing) return;
        
        this.isEditing = true;
        const todoItem = document.querySelector(`[data-id="${id}"]`);
        const textSpan = todoItem.querySelector('.todo-text');
        const editInput = todoItem.querySelector('.todo-edit-input');
        const editBtn = todoItem.querySelector('.edit-btn');
        const deleteBtn = todoItem.querySelector('.delete-btn');
        const saveBtn = todoItem.querySelector('.save-btn');
        const cancelBtn = todoItem.querySelector('.cancel-btn');
        
        textSpan.style.display = 'none';
        editInput.style.display = 'block';
        editInput.value = textSpan.textContent;
        editInput.focus();
        
        editBtn.style.display = 'none';
        deleteBtn.style.display = 'none';
        saveBtn.style.display = 'inline-block';
        cancelBtn.style.display = 'inline-block';
    }

    // Todo編集保存
    async saveTodoEdit(id) {
        const todoItem = document.querySelector(`[data-id="${id}"]`);
        const editInput = todoItem.querySelector('.todo-edit-input');
        const newText = editInput.value.trim();
        
        if (!newText) {
            this.cancelTodoEdit(id);
            return;
        }

        try {
            const response = await this.apiCall(`/api/todos/${id}`, {
                text: newText
            }, 'PUT');

            if (response.success) {
                const todo = this.todos.find(t => t.id === id);
                if (todo) {
                    todo.text = newText;
                    this.updateTodoDisplay();
                }
            }
        } catch (error) {
            console.error('Todo編集エラー:', error);
        } finally {
            this.isEditing = false;
        }
    }

    // Todo編集キャンセル
    cancelTodoEdit(id) {
        this.isEditing = false;
        this.updateTodoDisplay();
    }

    // Todo削除
    async deleteTodo(id) {
        if (!confirm('このToDoを削除しますか？')) return;

        try {
            const response = await this.apiCall(`/api/todos/${id}`, {}, 'DELETE');

            if (response.success) {
                this.todos = this.todos.filter(t => t.id !== id);
                this.updateTodoDisplay();
            }
        } catch (error) {
            console.error('Todo削除エラー:', error);
        }
    }

    // フィルター変更
    handleFilterChange(e) {
        const filter = e.target.dataset.filter;
        this.currentFilter = filter;
        
        this.filterBtns.forEach(btn => btn.classList.remove('active'));
        e.target.classList.add('active');
        
        this.updateTodoDisplay();
    }

    // Todo表示更新
    updateTodoDisplay() {
        const filteredTodos = this.getFilteredTodos();
        
        this.todoList.innerHTML = '';
        
        if (filteredTodos.length === 0) {
            this.emptyState.style.display = 'block';
            this.todoList.style.display = 'none';
        } else {
            this.emptyState.style.display = 'none';
            this.todoList.style.display = 'block';
            
            filteredTodos.forEach(todo => {
                const todoElement = this.createTodoElement(todo);
                this.todoList.appendChild(todoElement);
            });
        }
        
        this.updateTodoCount();
    }

    // フィルター済みTodo取得
    getFilteredTodos() {
        switch (this.currentFilter) {
            case 'completed':
                return this.todos.filter(t => t.completed);
            case 'pending':
                return this.todos.filter(t => !t.completed);
            default:
                return this.todos;
        }
    }

    // Todo要素作成
    createTodoElement(todo) {
        const template = this.todoItemTemplate.content.cloneNode(true);
        const li = template.querySelector('.todo-item');
        const checkbox = template.querySelector('.todo-checkbox');
        const textSpan = template.querySelector('.todo-text');
        const editInput = template.querySelector('.todo-edit-input');
        const editBtn = template.querySelector('.edit-btn');
        const deleteBtn = template.querySelector('.delete-btn');
        const saveBtn = template.querySelector('.save-btn');
        const cancelBtn = template.querySelector('.cancel-btn');
        
        li.dataset.id = todo.id;
        if (todo.completed) {
            li.classList.add('completed');
        }
        
        checkbox.checked = todo.completed;
        textSpan.textContent = todo.text;
        editInput.value = todo.text;
        
        // イベントリスナー追加
        checkbox.addEventListener('change', () => this.toggleTodo(todo.id));
        editBtn.addEventListener('click', () => this.startEditTodo(todo.id));
        deleteBtn.addEventListener('click', () => this.deleteTodo(todo.id));
        saveBtn.addEventListener('click', () => this.saveTodoEdit(todo.id));
        cancelBtn.addEventListener('click', () => this.cancelTodoEdit(todo.id));
        
        // Enterキーで保存、Escapeキーでキャンセル
        editInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.saveTodoEdit(todo.id);
            } else if (e.key === 'Escape') {
                e.preventDefault();
                this.cancelTodoEdit(todo.id);
            }
        });
        
        return li;
    }

    // Todo件数更新
    updateTodoCount() {
        const totalCount = this.todos.length;
        const completedCount = this.todos.filter(t => t.completed).length;
        const pendingCount = totalCount - completedCount;
        
        switch (this.currentFilter) {
            case 'completed':
                this.todoCount.textContent = completedCount;
                break;
            case 'pending':
                this.todoCount.textContent = pendingCount;
                break;
            default:
                this.todoCount.textContent = totalCount;
        }
    }

    // ローディング表示制御
    showLoading(show) {
        this.loadingDiv.style.display = show ? 'block' : 'none';
    }

    // APIコール
    async apiCall(url, data = {}, method = 'GET') {
        try {
            const options = {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'same-origin'
            };
            
            if (method !== 'GET' && Object.keys(data).length > 0) {
                options.body = JSON.stringify(data);
            }
            
            const response = await fetch(url, options);
            
            if (response.status === 401) {
                // 認証エラーの場合、ログインページにリダイレクト
                window.location.href = '/login';
                return;
            }
            
            return await response.json();
        } catch (error) {
            console.error('API call error:', error);
            throw error;
        }
    }
}

// アプリケーション初期化（認証不要）
document.addEventListener('DOMContentLoaded', () => {
    new TodoApp();
});