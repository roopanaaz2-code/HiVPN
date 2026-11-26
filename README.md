# HiVPN

A free-first Android VPN project built around real WireGuard gateways.

> **Project status:** Planning complete (M0). No production VPN server or functional APK has been deployed yet.

## Project goals
- Android-first VPN client.
- WireGuard-based encrypted tunnels.
- Free-to-use launch target, constrained by verified server capacity.
- Multiple countries added only when real gateways are deployed, tested, and online.
- Accurate connection status, server health, and measured latency.
- Privacy-first design with minimal operational data collection.

## Important
A zero-dollar hosting budget cannot guarantee many countries, unlimited bandwidth, or production-grade uptime. Free cloud resources have region, capacity, account, and egress limits. HiVPN will not display fake locations, fake ping values, or simulated connected states.

## Roadmap
See [docs/PROJECT_PLAN.md](docs/PROJECT_PLAN.md) for architecture, security requirements, hosting feasibility, milestones, and production release gates.

1. M0 — Technical planning and feasibility
2. M1 — Android project scaffold and CI
3. M2 — One real WireGuard gateway
4. M3 — Secure provisioning backend
5. M4 — Android VPN tunnel integration
6. M5 — Security, leak, resilience, and performance testing
7. M6 — UI polish and animations
8. M7 — Signed production APK and staged release

## Technology direction
- Kotlin and Android SDK
- Android `VpnService`
- WireGuard Android tunnel library
- Ubuntu LTS WireGuard gateway
- HTTPS control-plane API
- GitHub Actions for build and release automation

## Safety and privacy
VPN use is subject to local law and provider terms. A VPN changes the public network egress IP; it does not change GPS coordinates or guarantee anonymity. Do not commit private keys, server credentials, signing keys, or user tunnel configurations to this repository.


## VPN Gate community relay discovery (M1)

HiVPN can retrieve the official VPN Gate CSV API (`https://www.vpngate.net/api/iphone/`) and display volunteer-operated relays whose OpenVPN profiles pass basic structural and safety validation. These are third-party community servers, not HiVPN-owned infrastructure. Availability, performance, and operator logging practices vary. This milestone does **not** implement an OpenVPN tunnel yet; the app must not claim to be connected until a real tunnel is integrated and independently verified.
