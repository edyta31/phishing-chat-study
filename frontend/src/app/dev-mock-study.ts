/**
 * Local UI preview only — no backend calls for /study when enabled.
 * Set to `true` while checking layout; set to `false`
 * before building for production.
 */
export const MOCK_STUDY_LOCAL = false;

export const MOCK_NEXT_TASK_RESULT = {
  done: false as const,
  task: {
    trialId: 1,
    taskId: 1,
    kind: 'email',
    title: 'Sample task (local preview)',
    payload:
      'From: security@example.com\n' +
      'Subject: Verify your account\n\n' +
      'Dear customer,\n\n' +
      'We noticed unusual activity. Please confirm your details within 24 hours:\n' +
      'http://example-verify.test/login\n\n' +
      'Thank you,\n' +
      'Support Team'
  },
  currentIndex: 0,
  totalTasks: 1
};
