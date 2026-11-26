# HiVPN Project Plan

## Mission
Build a genuinely functional, privacy-conscious Android VPN client. The app is free to use at launch, subject to available server capacity. A server is shown as available only when its control-plane health check and VPN tunnel checks pass.

## Confirmed decisions
- Product: HiVPN
- First platform: Android
- Initial hosting budget: USD $0
- Desired reach: multiple countries, added only when real hosting is available and verified
- Initial community-network protocol: OpenVPN profiles supplied by VPN Gate's official CSV API (M1 discovery only; tunnel integration pending).\n- Future project-owned gateway protocol: WireGuard.
- Repository: roopanaaz2-code/HiVPN
- Build/release: GitHub Actions; signed release artifacts must use protected GitHub secrets.

## Critical feasibility note
A zero-dollar budget cannot guarantee a large, globally distributed, high-bandwidth VPN. Free cloud offers are limited by region, account eligibility, capacity, egress quotas, acceptable-use policies, and possible reclamation. VPN traffic can exhaust small free egress allowances quickly. Do not advertise unlimited bandwidth, guaranteed uptime, or countries without deployed gateways. Never use random public proxy/VPN endpoints.

## Proposed architecture
1. Android client (Kotlin, Android SDK): UI, country/server list, connection state, foreground VPN service, notification, permissions, secure configuration handling.
2. Tunnel engines: OpenVPN Android engine for VPN Gate community relays (licence audit required); official WireGuard Android tunnel library for future project-owned gateways. No custom cryptography.
3. Control plane: small HTTPS API for server catalog, health status, short-lived per-device peer provisioning, key revocation, and abuse/rate controls. Keep server private keys off the client and out of Git.
4. Gateway: Ubuntu LTS VM with WireGuard, IP forwarding, firewall, NAT, DNS resolver, monitoring, automatic security updates, and documented recovery.
5. Operations: health probes, latency measurement, capacity limits, abuse response, incident process, privacy policy, and transparent server status.
6. CI: reproducible Gradle build, unit/static checks, debug APK for testing, and signed release APK only after signing secrets are configured.

## Android implementation requirements
- Use Android VpnService with a legally compatible OpenVPN engine for VPN Gate relays; retain WireGuard support for project-owned gateways.
- Ask for Android VPN consent before connecting.
- Run a foreground service with persistent notification while connected.
- Model states explicitly: disconnected, requesting_permission, connecting, connected, reconnecting, disconnecting, error.
- The UI must reflect actual tunnel/backend state, never animation timers.
- Store private keys using Android Keystore-backed protection where feasible; never log credentials or tunnel configs.
- Respect Android lifecycle, network changes, battery limits, and VPN revocation.
- Provide an emergency disconnect and clear failure messages.
- Avoid claiming GPS/SIM location changes: VPN changes the public network egress IP only.

## Free-first server strategy
- Start with one gateway in a region where a genuinely free, policy-compliant VM is available to the project owner.
- Oracle Cloud Always Free is a candidate, not a guarantee: Always Free compute is tied to the account's home region and capacity can be unavailable. Verify current terms and eligibility before deployment.
- Google Cloud's free e2-micro offer is limited to specified US regions and includes only a small monthly outbound data allowance; unsuitable for general public VPN traffic.
- AWS free offers/credits are time-limited and account-dependent; do not treat them as permanent hosting.
- Add countries only after verifying provider terms, VM availability, public IPv4, UDP reachability, egress quotas, and measured capacity.
- Never deploy a public gateway without rate limits, abuse controls, security updates, and a clear provider acceptable-use review.

## Privacy and security baseline
- Minimize account data; no browsing-history or destination logging.
- Collect only operational metrics required for security and reliability, with retention disclosed.
- Encrypt control-plane traffic with HTTPS.
- Authenticate device provisioning; use short-lived enrollment tokens and revokeable peer keys.
- Keep secrets in server secret storage / GitHub Actions secrets, never source control.
- Define DNS behavior and test IPv4, IPv6, and DNS leak paths before release.
- Do not promise anonymity or protection against all tracking.
- Publish a privacy policy and contact/abuse process before public distribution.
- Check applicable laws and hosting-provider terms in each deployment jurisdiction.

## Release gates (must pass before calling it production-ready)
1. Build: clean CI build and install on supported Android versions.
2. Tunnel: successful WireGuard handshake to a gateway controlled by this project.
3. Routing: public egress IP matches selected gateway; ordinary browsing works.
4. DNS/IP: leak tests for DNS, IPv4, and IPv6; document unsupported IPv6 behavior rather than silently leaking.
5. Resilience: disconnect/reconnect, Wi-Fi-to-mobile handover, screen-off operation, process death, and VPN permission revocation.
6. Security: no secrets in APK/repository/logs; server firewall and peer revocation verified.
7. Performance: real latency, throughput, packet loss, and resource use measured under load.
8. Operations: health status, capacity limits, abuse response, and incident recovery verified.

## Milestones
- M0: planning and feasibility (this document)
- M1: Android project scaffold and CI build
- M2: single real WireGuard gateway provisioned and tested
- M3: secure device provisioning/control-plane API
- M4: Android tunnel integration and truthful connection states
- M5: leak, resilience, performance, and security tests
- M6: visual polish, icon, animations, accessibility, and battery tuning
- M7: signed APK, release notes, privacy policy, and staged rollout

## Current status
M0 planning documented. No VPN gateway has been provisioned; no working tunnel or APK exists yet. Do not mark connection functionality complete until the release gates above pass.
