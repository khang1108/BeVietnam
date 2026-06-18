# Deploy the web to Azure (Web App for Containers + ACR + GitHub Actions)

CI builds the Next.js image in Azure Container Registry and points an App Service
Web App at the new tag. `NEXT_PUBLIC_*` values are inlined at build time, so they
are passed as Docker build args from GitHub variables/secrets.

## 1. One-time Azure setup (run locally with `az login` first)

```bash
# --- pick names (ACR + app name must be globally unique, lowercase) ---
SUB_ID=$(az account show --query id -o tsv)
RG=bevietnam-rg
LOC=southeastasia
ACR=bevietnamacr            # -> bevietnamacr.azurecr.io
PLAN=bevietnam-plan
APP=bevietnam-web           # -> https://bevietnam-web.azurewebsites.net

az group create -n "$RG" -l "$LOC"
az acr create  -n "$ACR" -g "$RG" --sku Basic
az appservice plan create -n "$PLAN" -g "$RG" --is-linux --sku B1

# Create the Web App with a throwaway image (CI replaces it on first run)
az webapp create -g "$RG" -p "$PLAN" -n "$APP" \
  --deployment-container-image-name mcr.microsoft.com/azuredocs/aci-helloworld

# Container listens on 3000
az webapp config appsettings set -g "$RG" -n "$APP" --settings WEBSITES_PORT=3000

# Let the Web App pull from ACR via managed identity (no stored registry password)
az webapp identity assign -g "$RG" -n "$APP"
APP_PID=$(az webapp identity show -g "$RG" -n "$APP" --query principalId -o tsv)
ACR_ID=$(az acr show -n "$ACR" -g "$RG" --query id -o tsv)
az role assignment create --assignee "$APP_PID" --role AcrPull --scope "$ACR_ID"
az webapp config set -g "$RG" -n "$APP" \
  --generic-configurations '{"acrUseManagedIdentityCreds": true}'
```

## 2. Service principal for GitHub Actions

```bash
az ad sp create-for-rbac --name bevietnam-gh-deploy \
  --role contributor \
  --scopes "/subscriptions/$SUB_ID/resourceGroups/$RG" \
  --sdk-auth
```

Copy the whole JSON output into the GitHub secret `AZURE_CREDENTIALS`.

## 3. GitHub repo configuration

Settings → Secrets and variables → Actions.

**Variables** (not secret):

| Name | Example |
|---|---|
| `ACR_NAME` | `bevietnamacr` |
| `AZURE_RESOURCE_GROUP` | `bevietnam-rg` |
| `AZURE_WEBAPP_NAME` | `bevietnam-web` |
| `NEXT_PUBLIC_API_URL` | `https://api.iamphuckhang.dev/api/v1` |

**Secrets:**

| Name | Value |
|---|---|
| `AZURE_CREDENTIALS` | the `--sdk-auth` JSON from step 2 |
| `NEXT_PUBLIC_GOONG_API_KEY` | your Goong **Maptiles** key |

`NEXT_PUBLIC_API_URL` points at the backend once it is deployed (the AI service is
separate, served at `api.iamphuckhang.dev` from the VM). Update the variable and
re-run the workflow whenever the backend URL changes — it is baked into the image.

## 4. Deploy

Push to `main` touching `services/web/**`, or run the **Deploy web to Azure**
workflow manually (`workflow_dispatch`). The workflow:

1. `az acr build` — builds the image in ACR with the `NEXT_PUBLIC_*` build args.
2. `az webapp config container set` — repoints the Web App at the new `:<sha>` tag.
3. `az webapp restart`.

App URL: `https://<AZURE_WEBAPP_NAME>.azurewebsites.net`.

## 5. Custom domain (optional)

```bash
az webapp config hostname add -g "$RG" --webapp-name "$APP" --hostname www.iamphuckhang.dev
az webapp config ssl create  -g "$RG" --name "$APP" --hostname www.iamphuckhang.dev   # managed cert
```

Point a CNAME from your domain to `<APP>.azurewebsites.net` first.

## Local build check

```bash
docker build -t bevietnam-web services/web \
  --build-arg NEXT_PUBLIC_API_URL=http://localhost:8000/api/v1 \
  --build-arg NEXT_PUBLIC_GOONG_API_KEY=dummy
docker run -p 3000:3000 bevietnam-web
```
