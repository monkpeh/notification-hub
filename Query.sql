INSERT INTO public.campaign (name, business_purpose, status, owner)
VALUES ('Test Campaign', 'Sanity check', 'ACTIVE', 'you');

SELECT * FROM public.campaign;

DELETE FROM public.campaign WHERE name = 'Test Campaign';

INSERT INTO public.campaign (name, status)
VALUES ('Constraint Test', 'ACTIVE')
RETURNING id;

INSERT INTO public.template (campaign_id, is_parent, template_name, status)
VALUES (2, true, 'Constraint Test Template', 'Y')
RETURNING id;

INSERT INTO public.template_config (template_id, communication_medium, use_active)
VALUES (<template_id_from_above>, 'EMAIL', true);

INSERT INTO public.template_config (template_id, communication_medium, use_active)
VALUES (<template_id_from_above>, 'EMAIL', true);