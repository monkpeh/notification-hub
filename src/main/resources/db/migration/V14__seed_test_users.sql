INSERT INTO app_user (pid, full_name, email, role_id) VALUES
  ('super1', 'Super Admin Test', 'super1@test.local', (SELECT id FROM app_role WHERE name = 'SUPER_ADMIN')),
  ('admin1', 'Admin Test', 'admin1@test.local', (SELECT id FROM app_role WHERE name = 'ADMIN')),
  ('builder1', 'Template Builder Test', 'builder1@test.local', (SELECT id FROM app_role WHERE name = 'TEMPLATE_BUILDER')),
  ('approver1', 'Approver Test', 'approver1@test.local', (SELECT id FROM app_role WHERE name = 'APPROVER')),
  ('agent1', 'AI Agent Test', 'agent1@test.local', (SELECT id FROM app_role WHERE name = 'AI_AGENT'));
