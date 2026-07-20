CREATE TABLE ewe_tasks (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, instance_id uuid NOT NULL, task_key varchar(120) NOT NULL, name varchar(180) NOT NULL,
 task_type varchar(20) NOT NULL, task_status varchar(20) NOT NULL, assignment_type varchar(20) NOT NULL,
 assignee_user_id uuid, assignee_role varchar(120), assignee_group varchar(120), routing_expression varchar(1000), claimed_by uuid,
 delegated_from uuid, delegated_to uuid, due_at timestamptz, escalation_at timestamptz, completed_at timestamptz,
 outcome varchar(40), completion_comment varchar(1000), created_at timestamptz NOT NULL, created_by varchar(120) NOT NULL,
 updated_at timestamptz NOT NULL, updated_by varchar(120) NOT NULL, version bigint NOT NULL, status varchar(20) NOT NULL
);
CREATE INDEX ix_ewe_task_tenant_status ON ewe_tasks(tenant_id,task_status);
CREATE INDEX ix_ewe_task_assignee ON ewe_tasks(tenant_id,assignee_user_id);
CREATE INDEX ix_ewe_task_instance ON ewe_tasks(tenant_id,instance_id);
CREATE TABLE ewe_task_events (id uuid PRIMARY KEY,tenant_id uuid NOT NULL,task_id uuid NOT NULL,event_type varchar(50) NOT NULL,actor_user_id uuid,details jsonb NOT NULL DEFAULT '{}'::jsonb,occurred_at timestamptz NOT NULL);
CREATE INDEX ix_ewe_task_event ON ewe_task_events(tenant_id,task_id,occurred_at);
CREATE TABLE ewe_routing_rules (id uuid PRIMARY KEY,tenant_id uuid NOT NULL,workflow_definition_id uuid NOT NULL,task_key varchar(120) NOT NULL,priority integer NOT NULL,condition_expression varchar(1000),assignment_type varchar(20) NOT NULL,assignment_value varchar(500) NOT NULL,active boolean NOT NULL DEFAULT true,created_at timestamptz NOT NULL,created_by varchar(120) NOT NULL,updated_at timestamptz NOT NULL,updated_by varchar(120) NOT NULL,version bigint NOT NULL,status varchar(20) NOT NULL,CONSTRAINT uk_ewe_route UNIQUE(tenant_id,workflow_definition_id,task_key,priority));
CREATE INDEX ix_ewe_route_lookup ON ewe_routing_rules(tenant_id,workflow_definition_id,task_key,active,priority);
REVOKE UPDATE, DELETE ON ewe_task_events FROM PUBLIC;
