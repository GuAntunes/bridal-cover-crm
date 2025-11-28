{{/*
Expand the name of the chart.
*/}}
{{- define "bridal-cover-crm.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "bridal-cover-crm.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "bridal-cover-crm.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "bridal-cover-crm.labels" -}}
helm.sh/chart: {{ include "bridal-cover-crm.chart" . }}
{{ include "bridal-cover-crm.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: bridal-cover-crm
{{- end }}

{{/*
Selector labels
*/}}
{{- define "bridal-cover-crm.selectorLabels" -}}
app.kubernetes.io/name: {{ include "bridal-cover-crm.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "bridal-cover-crm.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "bridal-cover-crm.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Database host
*/}}
{{- define "bridal-cover-crm.databaseHost" -}}
{{- if .Values.postgresql.enabled }}
{{- printf "%s-postgresql" (include "bridal-cover-crm.fullname" .) }}
{{- else }}
{{- required "A valid database host is required when postgresql is disabled" .Values.externalDatabase.host }}
{{- end }}
{{- end }}

{{/*
Database port
*/}}
{{- define "bridal-cover-crm.databasePort" -}}
{{- if .Values.postgresql.enabled }}
{{- print "5432" }}
{{- else }}
{{- required "A valid database port is required when postgresql is disabled" .Values.externalDatabase.port }}
{{- end }}
{{- end }}

{{/*
Database name
*/}}
{{- define "bridal-cover-crm.databaseName" -}}
{{- if .Values.postgresql.enabled }}
{{- .Values.postgresql.auth.database }}
{{- else }}
{{- required "A valid database name is required when postgresql is disabled" .Values.externalDatabase.database }}
{{- end }}
{{- end }}

{{/*
Database username
*/}}
{{- define "bridal-cover-crm.databaseUsername" -}}
{{- if .Values.postgresql.enabled }}
{{- .Values.postgresql.auth.username }}
{{- else }}
{{- required "A valid database username is required when postgresql is disabled" .Values.externalDatabase.username }}
{{- end }}
{{- end }}

{{/*
Database password secret name
*/}}
{{- define "bridal-cover-crm.databaseSecretName" -}}
{{- if .Values.postgresql.enabled }}
{{- if .Values.postgresql.auth.existingSecret }}
{{- .Values.postgresql.auth.existingSecret }}
{{- else }}
{{- printf "%s-postgresql" (include "bridal-cover-crm.fullname" .) }}
{{- end }}
{{- else }}
{{- required "A valid database secret is required when postgresql is disabled" .Values.externalDatabase.existingSecret }}
{{- end }}
{{- end }}

{{/*
Database password secret key
*/}}
{{- define "bridal-cover-crm.databaseSecretKey" -}}
{{- if .Values.postgresql.enabled }}
{{- print "password" }}
{{- else }}
{{- .Values.externalDatabase.secretKey | default "password" }}
{{- end }}
{{- end }}

{{/*
Database URL
*/}}
{{- define "bridal-cover-crm.databaseUrl" -}}
{{- printf "jdbc:postgresql://%s:%s/%s" (include "bridal-cover-crm.databaseHost" .) (include "bridal-cover-crm.databasePort" .) (include "bridal-cover-crm.databaseName" .) }}
{{- end }}

