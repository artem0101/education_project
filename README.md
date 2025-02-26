# Education project

Find task by id:
GET /api/v1/tasks/{id}

Find all tasks:
GET /api/v1/tasks

Create task:
POST /api/v1/tasks
{
	"title": "test title",
	"description": "test description",
	"userId": 2
}

Update task:
PUT /api/v1/tasks/{id}
{
"title": "test title",
"description": "test description",
"userId": 2
}

Remove task:
DELETE /api/v1/tasks/{id}
