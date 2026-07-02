function SocialConflictModal({ onConfirm }) {
  return (
    <div className="require-login-layer" role="dialog" aria-modal="true" aria-labelledby="social-conflict-title">
      <div className="require-login-backdrop" />
      <section className="require-login-modal">
        <div className="require-login-icon" aria-hidden="true">!</div>
        <h2 id="social-conflict-title">이미 가입된 회원입니다</h2>
        <p>동일한 이메일로 가입된 계정이 이미 있습니다.</p>
        <button className="primary" onClick={onConfirm}>확인</button>
      </section>
    </div>
  )
}

export default SocialConflictModal
