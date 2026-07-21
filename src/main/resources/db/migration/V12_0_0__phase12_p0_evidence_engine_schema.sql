-- Phase 12-P0 Clinical Evidence Engine schema

create table evidence_source (
  source_id varchar(160) primary key,
  display_name text not null,
  publisher text not null,
  source_type varchar(80) not null,
  authority_level varchar(40) not null,
  jurisdiction varchar(80) not null,
  language varchar(40) not null,
  homepage text,
  license_status varchar(40) not null,
  license_name text,
  license_reference text,
  trust_status varchar(40) not null,
  review_status varchar(40) not null,
  reviewed_at timestamptz,
  reviewed_by varchar(160),
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_evidence_source_review on evidence_source(review_status, license_status, trust_status);

create table evidence_asset_version (
  version_id varchar(160) primary key,
  asset_id varchar(160) not null,
  source_id varchar(160) not null references evidence_source(source_id),
  title text not null,
  document_type varchar(80),
  external_reference text,
  specialty varchar(120),
  intended_audience varchar(80),
  jurisdiction varchar(80),
  language varchar(40),
  publication_date date,
  effective_from timestamptz,
  effective_to timestamptz,
  supersedes_version_id varchar(160),
  lifecycle_status varchar(40) not null,
  review_status varchar(40) not null,
  checksum varchar(160) not null,
  mime_type varchar(120),
  content_length bigint not null default 0,
  parser_version varchar(120),
  schema_version varchar(80),
  ingested_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (asset_id, version_id)
);

create index idx_evidence_asset_source on evidence_asset_version(source_id);
create index idx_evidence_asset_lifecycle on evidence_asset_version(lifecycle_status, review_status);
create index idx_evidence_asset_effective on evidence_asset_version(effective_from, effective_to);

create table evidence_chunk (
  chunk_id varchar(180) primary key,
  version_id varchar(160) not null references evidence_asset_version(version_id),
  section_path text,
  ordinal integer not null,
  normalized_text text not null,
  text_checksum varchar(160) not null,
  token_count integer not null default 0,
  metadata jsonb not null default '{}'::jsonb
);

create index idx_evidence_chunk_version on evidence_chunk(version_id, ordinal);
create index idx_evidence_chunk_text_gin on evidence_chunk using gin (to_tsvector('simple', normalized_text));

create table evidence_span (
  span_id varchar(180) primary key,
  chunk_id varchar(180) not null references evidence_chunk(chunk_id),
  version_id varchar(160) not null references evidence_asset_version(version_id),
  start_offset integer not null,
  end_offset integer not null,
  quoted_text text not null,
  span_checksum varchar(160) not null,
  locator text,
  span_type varchar(80) not null,
  check (start_offset >= 0),
  check (end_offset > start_offset)
);

create index idx_evidence_span_chunk on evidence_span(chunk_id);
create index idx_evidence_span_version on evidence_span(version_id);

create table evidence_claim (
  claim_id varchar(180) primary key,
  version_id varchar(160) not null references evidence_asset_version(version_id),
  claim_type varchar(80) not null,
  normalized_claim text not null,
  intended_audience varchar(80) not null,
  tags jsonb not null default '[]'::jsonb,
  population text,
  intervention text,
  comparator text,
  outcome text,
  evidence_quality varchar(80) not null default 'UNRATED',
  recommendation_strength varchar(80) not null default 'UNRATED',
  review_status varchar(80) not null default 'DRAFT',
  origin_type varchar(80) not null default 'UNKNOWN',
  claim_checksum varchar(160) not null
);

create index idx_evidence_claim_version on evidence_claim(version_id);
create index idx_evidence_claim_type on evidence_claim(claim_type);

create table claim_evidence_link (
  link_id varchar(180) primary key,
  claim_id varchar(180) not null references evidence_claim(claim_id),
  span_id varchar(180) not null references evidence_span(span_id),
  support_status varchar(80) not null,
  rationale text
);

create index idx_claim_evidence_link_claim on claim_evidence_link(claim_id);
create index idx_claim_evidence_link_span on claim_evidence_link(span_id);

create table citation_verification_result (
  verification_id varchar(180) primary key,
  claim_id varchar(180) not null references evidence_claim(claim_id),
  span_id varchar(180) not null references evidence_span(span_id),
  support_status varchar(80) not null,
  provider_id varchar(160) not null,
  provider_version varchar(120) not null,
  rationale text,
  verified_at timestamptz not null
);

create index idx_citation_verification_claim on citation_verification_result(claim_id);

create table evidence_conflict_set (
  conflict_id varchar(180) primary key,
  conflict_type varchar(80) not null,
  claim_ids jsonb not null default '[]'::jsonb,
  summary text not null,
  review_required boolean not null default true,
  created_at timestamptz not null default now()
);

create table evidence_conflict_member (
  conflict_id varchar(180) not null references evidence_conflict_set(conflict_id),
  claim_id varchar(180) not null references evidence_claim(claim_id),
  primary key (conflict_id, claim_id)
);

create table retrieval_trace (
  trace_id varchar(180) primary key,
  retrieval_id varchar(180) not null unique,
  request_id varchar(180) not null,
  retrieval_scope varchar(80) not null,
  provider_id varchar(160) not null,
  provider_version varchar(120) not null,
  query_summary jsonb not null,
  eligible_version_ids jsonb not null default '[]'::jsonb,
  matched_claim_ids jsonb not null default '[]'::jsonb,
  rejected_claim_ids jsonb not null default '[]'::jsonb,
  warnings jsonb not null default '[]'::jsonb,
  created_at timestamptz not null
);

create index idx_retrieval_trace_request on retrieval_trace(request_id);
create index idx_retrieval_trace_created_at on retrieval_trace(created_at);

create table retrieval_candidate_trace (
  candidate_trace_id varchar(180) primary key,
  retrieval_id varchar(180) not null references retrieval_trace(retrieval_id),
  claim_id varchar(180),
  source_id varchar(160),
  asset_id varchar(160),
  version_id varchar(160),
  chunk_id varchar(180),
  span_id varchar(180),
  lexical_score numeric(8,4),
  dense_score numeric(8,4),
  fusion_score numeric(8,4),
  final_score numeric(8,4),
  decision varchar(80) not null,
  reasons jsonb not null default '[]'::jsonb
);

create index idx_retrieval_candidate_trace_retrieval on retrieval_candidate_trace(retrieval_id);
create index idx_retrieval_candidate_trace_claim on retrieval_candidate_trace(claim_id);

create table embedding_index_metadata (
  index_id varchar(180) primary key,
  version_id varchar(160) not null references evidence_asset_version(version_id),
  provider_id varchar(160) not null,
  provider_version varchar(120) not null,
  embedding_model varchar(160) not null,
  dimension integer not null,
  status varchar(80) not null,
  created_at timestamptz not null default now(),
  metadata jsonb not null default '{}'::jsonb
);

create table evidence_chunk_embedding (
  embedding_id varchar(180) primary key,
  index_id varchar(180) not null references embedding_index_metadata(index_id),
  chunk_id varchar(180) not null references evidence_chunk(chunk_id),
  embedding jsonb not null,
  created_at timestamptz not null default now(),
  unique (index_id, chunk_id)
);
