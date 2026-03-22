# Study frontend (Angular)

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 20.3.9.

**Styling:** [Tailwind CSS v4](https://tailwindcss.com/) with `@tailwindcss/postcss` (see `postcss.config.json` and `src/styles.css`; Angular CLI only auto-loads JSON PostCSS config). Fonts: **Plus Jakarta Sans** (UI) and **Fraunces** (headings), loaded from Google Fonts in `src/index.html`.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

### Local preview without the backend

To check how the study UI looks (one static task, no API calls), set `MOCK_STUDY_LOCAL` to `true` in `src/app/dev-mock-study.ts`, then open `http://localhost:4200/study`. Edit `MOCK_NEXT_TASK_RESULT` in that file to change the sample content. Set it back to `false` before deploying or when a want a real run.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile the project and store the build artifacts in the `dist/` directory. By default, the production build optimizes the application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```
