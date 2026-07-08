export function SymptomIntakePage() {
  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Symptom Intake</h1>
        <p>P0 演示表单只用于本地展示，不提交真实病历或外部系统。</p>
      </div>
      <form className="portal-form">
        <label>
          主要不适
          <textarea defaultValue="胸口不适，活动后更明显。" />
        </label>
        <label>
          大约开始时间
          <input defaultValue="今天上午" />
        </label>
        <label>
          当前安全状态
          <select defaultValue="watching">
            <option value="watching">继续观察并补充信息</option>
            <option value="urgent">已出现紧急信号，准备急救</option>
          </select>
        </label>
      </form>
    </section>
  );
}
