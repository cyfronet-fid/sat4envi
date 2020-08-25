# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
The most recent changes are on top, in each type of changes category.

## [Unreleased]

### Added

- Categories in sidebar [#599](https://github.com/cyfronet-fid/sat4envi/issues/599)
- Add Products seed script [#665](https://github.com/cyfronet-fid/sat4envi/issues/665)
- E2e tests [#674](https://github.com/cyfronet-fid/sat4envi/issues/674)
- Endpoint returning the most recent scene for a product [#680](https://github.com/cyfronet-fid/sat4envi/issues/680)

### Changed

- Remove Group entity [#669](https://github.com/cyfronet-fid/sat4envi/issues/669)

### Fixed

### Updated

- 2020.09 backend dependencies update

## [v11.0.0]

### Added

- HTTP code 429 for /login and /token endpoints [#541](https://github.com/cyfronet-fid/sat4envi/issues/541)
- Overlay management [#615](https://github.com/cyfronet-fid/sat4envi/issues/615)
- Add operations on created invitations and status of them [#643](https://github.com/cyfronet-fid/sat4envi/issues/643)
- Admin scenes synchronization endpoints [#621](https://github.com/cyfronet-fid/sat4envi/issues/621)
- Create endpoints for existing invitations [#642](https://github.com/cyfronet-fid/sat4envi/issues/642)
- Admin endpoints for Scenes management [#620](https://github.com/cyfronet-fid/sat4envi/issues/620)
- Confirm or reject invitation with email URLs [#625](https://github.com/cyfronet-fid/sat4envi/issues/625)

### Fixed

- Fix resending invitations and refactor endpoints to not operate on tokens [#660](https://github.com/cyfronet-fid/sat4envi/issues/660)
- Fix circular dependencies while dependency injection [#648](https://github.com/cyfronet-fid/sat4envi/issues/648)
- Super admin security issues [#551](https://github.com/cyfronet-fid/sat4envi/issues/551)

## [v10.0.0]

### Added

- Basic institution invitation endpoint [#608](https://github.com/cyfronet-fid/sat4envi/issues/608)
- Admin endpoints for Schema and Product management [#579](https://github.com/cyfronet-fid/sat4envi/issues/579)
- Query param handling in search endpoint [#512](https://github.com/cyfronet-fid/sat4envi/issues/512)
- Add keys handling for search [#556](https://github.com/cyfronet-fid/sat4envi/issues/556)
- Overlay Management UI [#614](https://github.com/cyfronet-fid/sat4envi/issues/614)

### Changed

- Trigger creation of views for GeoServer on DB level [#622](https://github.com/cyfronet-fid/sat4envi/issues/622)

### Fixed

- Improve DB normalization [#623](https://github.com/cyfronet-fid/sat4envi/issues/623)
- Improve scene time filtering performance [#610](https://github.com/cyfronet-fid/sat4envi/issues/610)

## [v9.1.0]

### Added

- Create events directive [#592](https://github.com/cyfronet-fid/sat4envi/issues/592)

### Changed

- Set GeoServer mosaic levels and heterogeneous properties [#603](https://github.com/cyfronet-fid/sat4envi/issues/603)

### Fixed

- Fix no search results info [#601](https://github.com/cyfronet-fid/sat4envi/issues/601)

## [v9.0.0]

### Added

- Add footprint, artifacts and metadata to search response [#581](https://github.com/cyfronet-fid/sat4envi/issues/581)
- Cookie based authorization [#583](https://github.com/cyfronet-fid/sat4envi/issues/583)
- Add required field to institutions seeds [#496](https://github.com/cyfronet-fid/sat4envi/issues/496)
- Sentinel API for OpenSearch and OData download [#67](https://github.com/cyfronet-fid/sat4envi/issues/67)
- Static Modal For Search Result Details [#549](https://github.com/cyfronet-fid/sat4envi/issues/549)
- Add static product info box for selected product [#545](https://github.com/cyfronet-fid/sat4envi/issues/545)
- Add option to unselect selected product
- Create dynamic breadcrumbs and add general breadcrumb in /settings [#503](https://github.com/cyfronet-fid/sat4envi/issues/503)
- Add authorization token to image wms layer requests [#534](https://github.com/cyfronet-fid/sat4envi/issues/534)
- Favourites Tab in products navigation, loading spinners for products [539](https://github.com/cyfronet-fid/sat4envi/issues/539)
- Connect sentinel search to backend + add forms [#532](https://github.com/cyfronet-fid/sat4envi/issues/532)
- Generic loader for HTTP requests, navigation events and `open layer` map loading on `/map/products` site [#217](https://github.com/cyfronet-fid/sat4envi/issues/217)
- Institutions and groups improvement HTML/CSS [#493](https://github.com/cyfronet-fid/sat4envi/issues/493)
- Change add group and add person to modal [#508](https://github.com/cyfronet-fid/sat4envi/issues/508)
- Create separate change password page [#511](https://github.com/cyfronet-fid/sat4envi/issues/511)
- Add generic list view, overhoul institution list view [#456](https://github.com/cyfronet-fid/sat4envi/issues/456)
- Add Sentinel search config endpoint [#485](https://github.com/cyfronet-fid/sat4envi/issues/485)
- Preset gs-gateway configurations per dataset
- Add Sentinel search config endpoint [#485](https://github.com/cyfronet-fid/sat4envi/issues/485)
- Edit institution [#494](https://github.com/cyfronet-fid/sat4envi/issues/494)
- GeoServer Gateway [#479](https://github.com/cyfronet-fid/sat4envi/issues/479)
- Add parentName to basic institution response
- Add Institution form for superadmin [#470](https://github.com/cyfronet-fid/sat4envi/issues/470)
- Add institution selection into super Admin and Admin settings page [#455](https://github.com/cyfronet-fid/sat4envi/issues/455)

### Changed

- Set secure flag in token cookie and configure nginx certificate [#589](https://github.com/cyfronet-fid/sat4envi/issues/589)
- Update dataset s4e-sync-1 [#578](https://github.com/cyfronet-fid/sat4envi/issues/578)
- Move authorization to WebSecurity [#481](https://github.com/cyfronet-fid/sat4envi/issues/481)
- Remove dependence on Groups in PreAuthorized methods [#522](https://github.com/cyfronet-fid/sat4envi/issues/522)
- Improve search scenes endpoint
- Make s4e-backend use spring-boot BOM [#479](https://github.com/cyfronet-fid/sat4envi/issues/479)
- Refactor aside.js from sidebar [#477](https://github.com/cyfronet-fid/sat4envi/issues/477)
- Change requirement of admin institution email

### Fixed

- Sentinel Search Query [#568](https://github.com/cyfronet-fid/sat4envi/issues/568)
- [Fix] Hide `/settings` groups and it's functionalities [#571](https://github.com/cyfronet-fid/sat4envi/pull/571)
- resize `Warstwy` segment in `/map` sidebar, [Fix] Update UI messages [#566](https://github.com/cyfronet-fid/sat4envi/pull/566)
- Visual bugfixes [#576](https://github.com/cyfronet-fid/sat4envi/issues/576)
- Fix schema scanning and parallel seeding when running from a jar [#570](https://github.com/cyfronet-fid/sat4envi/pull/570)
- Fix Institution edition, registration form, add child institution and cancel password change [#558](https://github.com/cyfronet-fid/sat4envi/pull/558)
- [fix] dropdowns toggle on map and in settings [#562](https://github.com/cyfronet-fid/sat4envi/issues/562)
- Fix institution loading in search box
- Fix registration form payload [#537](https://github.com/cyfronet-fid/sat4envi/pull/538)
- Handle nulls in custom Validators [#491](https://github.com/cyfronet-fid/sat4envi/issues/491)
- Fix PUT endpoint for Institutions [#500](https://github.com/cyfronet-fid/sat4envi/issues/500)
- Fix and test refactor for /institutions endpoint [#472](https://github.com/cyfronet-fid/sat4envi/issues/472)

### Updated

- 2020.07 backend dependencies update
- 2020.06 backend dependencies update

## [v8.0.0]

### Added

- Select parent institution modal [#471](https://github.com/cyfronet-fid/sat4envi/issues/471)
- JS script with PoC - layers overlapping [#454](https://github.com/cyfronet-fid/sat4envi/issues/454)
- Scene metadata search [#422](https://github.com/cyfronet-fid/sat4envi/issues/422)
- Listen to scene files notifications from AMQP queue [#440](https://github.com/cyfronet-fid/sat4envi/issues/440)
- Add version display for provided env variable during build time [#459](https://github.com/cyfronet-fid/sat4envi/issues/459)
- Include commit SHA in maven project.version and pass it to npm [#461](https://github.com/cyfronet-fid/sat4envi/issues/461)

### Changed

- Extension of the groups/institution API [#472](https://github.com/cyfronet-fid/sat4envi/issues/472)
- Handle Scene updates in ScenePersister [#469](https://github.com/cyfronet-fid/sat4envi/issues/469)
- New sidebar look [#454](https://github.com/cyfronet-fid/sat4envi/issues/454)
- Improve institutions endpoint to work for any signed in user [#457](https://github.com/cyfronet-fid/sat4envi/issues/457)

### Updated

- Update springdoc-openapi to 1.3.9 and add a test openapi endpoint works [#467](https://github.com/cyfronet-fid/sat4envi/issues/467)

## [v7.0.0]

### Added

- Generic Tile View for admin and superadmin [#450](https://github.com/cyfronet-fid/sat4envi/issues/450)
- Add global error handling [#430](https://github.com/cyfronet-fid/sat4envi/issues/430)
- UI Refactoring [#448](https://github.com/cyfronet-fid/sat4envi/issues/448)
- Add /logout routing and clear storage on logout (inconsistent state of storage) [#445](https://github.com/cyfronet-fid/sat4envi/issues/445)
- Synchronize scenes from S3 (s4-sync-1 dataset) [#439](https://github.com/cyfronet-fid/sat4envi/issues/439)
- Audit selected entities [#158](https://github.com/cyfronet-fid/sat4envi/issues/158)

### Updated

- GeoServer image to fiddev/geoserver:1.4.0-GS2.17.0
- 2020.05 backend dependencies update

## [v6.0.0]

### Added

- Password change front [#158](https://github.com/cyfronet-fid/sat4envi/issues/158)
- SceneAcceptor driven by files from S3 [#160](https://github.com/cyfronet-fid/sat4envi/issues/160)
- Notifications module [#423](https://github.com/cyfronet-fid/sat4envi/issues/423)
- Notification for successful share configuration [#435](https://github.com/cyfronet-fid/sat4envi/issues/435)

### Changed

- Use RSA key-pair to sign and verify JWT tokens [#428](https://github.com/cyfronet-fid/sat4envi/issues/428)

### Fixed

- [Bugfix] places input should keep state [#427](https://github.com/cyfronet-fid/sat4envi/issues/427)

## [v5.1.0]

### Added

- Favorites products [#420](https://github.com/cyfronet-fid/sat4envi/issues/420)
- Favourite Products [#403](https://github.com/cyfronet-fid/sat4envi/issues/403)
- Forms module [#370](https://github.com/cyfronet-fid/sat4envi/issues/370)

### Changed

- Overflow for map, sidebar, modals [#359](https://github.com/cyfronet-fid/sat4envi/issues/359)
- Run e2e and integration tests in gh-actions and modify docker-compose files [#401](https://github.com/cyfronet-fid/sat4envi/issues/401)
- Add more seeds for tests [#411](https://github.com/cyfronet-fid/sat4envi/issues/411)
- Update handlebars version to 4.6.0

## [v5.0.0]

### Added

- Schema data model and CRD [#69](https://github.com/cyfronet-fid/sat4envi/issues/69)

### Changed

- Integrate Image Mosaic with TIME parameter [#389](https://github.com/cyfronet-fid/sat4envi/issues/389)
- Separate backend and web tests in gh-actions [#399](https://github.com/cyfronet-fid/sat4envi/issues/399)
- Rework backend Spring Security [#390](https://github.com/cyfronet-fid/sat4envi/issues/390)
- Login/Settings/User functionality changed to dropdown button [#386](https://github.com/cyfronet-fid/sat4envi/issues/386)

## [v4.0.0]

### Added

- Thumbnails MediaType and dimensions validation [#286](https://github.com/cyfronet-fid/sat4envi/issues/286)
- Data set: s4e-demo-2 [#369](https://github.com/cyfronet-fid/sat4envi/pull/369)
- Thumbnail scaling before POSTing in share view and save view [#341](https://github.com/cyfronet-fid/sat4envi/pull/367/files)
- PDF Report generation [#345](https://github.com/cyfronet-fid/sat4envi/pull/345)
- Timezone support for s4e-web [#363](https://github.com/cyfronet-fid/sat4envi/pull/363)
- DisplayName field to Product and appropriate view [#365](https://github.com/cyfronet-fid/sat4envi/pull/365)
- Migration with 'On delete cascade' for AppUser field on PasswordReset and EmailVerification tables [#364](https://github.com/cyfronet-fid/sat4envi/pull/364)
- Possibility to set maven artifact version with properties [#356](https://github.com/cyfronet-fid/sat4envi/pull/356)
- This changelog file [#355](https://github.com/cyfronet-fid/sat4envi/pull/355)

### Changed

- Modify Product and Scene to work with GeoServer ImageMosaic [#371](https://github.com/cyfronet-fid/sat4envi/pull/371)
- Update GeoServer image to 1.3.0-GS2.16.2, which includes JNDI and removes JDBCConfig support [#378](https://github.com/cyfronet-fid/sat4envi/pull/378)
- Update db image to postgis/postgis:12-3.0-alpine and use Postgis hibernate dialect [#371](https://github.com/cyfronet-fid/sat4envi/pull/371)
- Update GeoServer image to 1.2.0-GS2.16.2 and use URL-correct S3Geotiff endpoint [#371](https://github.com/cyfronet-fid/sat4envi/pull/371)

### Fixed

- Return site-key instead of secret-key in ConfigController [#381](https://github.com/cyfronet-fid/sat4envi/issues/381)
- Build in private scope [#372](https://github.com/cyfronet-fid/sat4envi/pull/372)
- Constraints names on Product and Scene tables [#364](https://github.com/cyfronet-fid/sat4envi/pull/364)

### Updates

- 2020.03 backend dependencies update

[unreleased]: https://github.com/cyfronet-fid/sat4envi/compare/v11.0.0...HEAD
[v11.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/v10.0.0...v11.0.0
[v10.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/v9.1.0...v10.0.0
[v9.1.0]: https://github.com/cyfronet-fid/sat4envi/compare/v9.0.0...v9.1.0
[v9.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/v8.0.0...v9.0.0
[v8.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/v7.0.0...v8.0.0
[v7.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/v6.0.0...v7.0.0
[v6.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/v5.1.0...v6.0.0
[v5.1.0]: https://github.com/cyfronet-fid/sat4envi/compare/v5.0.0...v5.1.0
[v5.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/v4.0.0...v5.0.0
[v4.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/0ebb1138...v4.0.0
